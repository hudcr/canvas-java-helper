const messagesEl = document.getElementById('messages');
const inputEl = document.getElementById('input');
const coursesEl = document.getElementById('courses');
const modal = document.getElementById('settingsModal');

const md = (txt) => txt
  .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  .replace(/\n/g, '<br/>');

function addMsg(role, text, context=[]) {
  const div = document.createElement('div');
  div.className = `msg ${role}`;
  div.innerHTML = `${md(text)}<div class="meta">${new Date().toLocaleTimeString()}${context.length ? ` · Context: ${context.join(', ')}`:''}</div>`;
  messagesEl.appendChild(div);
  messagesEl.scrollTop = messagesEl.scrollHeight;
}

async function loadCourses() {
  const res = await fetch('/api/courses');
  const data = await res.json();
  document.getElementById('upcoming').textContent = data.upcoming;
  document.getElementById('overdue').textContent = data.overdue;
  coursesEl.innerHTML = '';
  data.courses.forEach(c => {
    const li = document.createElement('li');
    li.textContent = `${c.name} (${c.code ?? 'No code'})`;
    li.onclick = () => loadAssignments(c.id, li);
    coursesEl.appendChild(li);
  });
}

async function loadAssignments(courseId, li) {
  document.querySelectorAll('#courses li').forEach(x=>x.style.outline='none');
  li.style.outline = '2px solid #dbeafe';
  const res = await fetch(`/api/assignments?courseId=${courseId}`);
  const as = await res.json();
  addMsg('assistant', `Loaded ${as.length} assignments for this course.`);
}

document.getElementById('sendBtn').onclick = async () => {
  const message = inputEl.value.trim();
  if (!message) return;
  addMsg('user', message);
  inputEl.value = '';
  const typing = document.createElement('div'); typing.className='msg'; typing.textContent='Thinking...'; messagesEl.appendChild(typing);
  const res = await fetch('/api/chat', {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({message, threadId:'default'})});
  const data = await res.json();
  typing.remove();
  addMsg('assistant', data.answer, data.contextTitles || []);
};

document.getElementById('syncBtn').onclick = async () => {
  addMsg('assistant', 'Sync started...');
  const res = await fetch('/api/sync', {method:'POST'});
  const data = await res.json();
  addMsg('assistant', `Sync complete. Courses: ${data.coursesSynced}, assignments: ${data.assignmentsSynced}, chunks updated: ${data.chunksUpdated}`);
  loadCourses();
};

document.getElementById('settingsBtn').onclick = async () => {
  const res = await fetch('/api/settings');
  const s = await res.json();
  [...document.querySelectorAll('#settingsForm input')].forEach(i => i.value = s[i.name] || '');
  modal.showModal();
};

document.getElementById('saveSettings').onclick = async (e) => {
  e.preventDefault();
  const form = new FormData(document.getElementById('settingsForm'));
  const payload = Object.fromEntries(form.entries());
  await fetch('/api/settings', {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload)});
  modal.close();
  addMsg('assistant', 'Settings saved.');
};

document.getElementById('toggleSidebar').onclick = () => document.getElementById('sidebar').classList.toggle('open');
loadCourses();
addMsg('assistant', 'Welcome! Sync Canvas, then ask questions about coursework and grades.');
