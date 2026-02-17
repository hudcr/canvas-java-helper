# Canvas Java Helper (Java 21 + Spring Boot 3)

A complete web app that syncs Canvas LMS data (courses, assignments, grade snapshots), stores it in H2, builds lightweight RAG embeddings with OpenAI, and provides a modern central chat UI.

## Features
- Canvas sync (`/api/sync`) with pagination + basic 429 retry
- H2 persistence with normalized tables
- RAG chunks + embeddings in DB
- Cosine similarity retrieval with due-date and recency boosting
- Chat endpoint (`/api/chat`) powered by OpenAI Chat Completions
- Clean white modern UI at `/`
- Settings modal (keys are server-side only and masked when read)
- Background sync every 6 hours (configurable)

## Setup
1. Install Java 21 and Maven.
2. Configure credentials in `src/main/resources/application.properties` or env vars:

```properties
canvas.baseUrl=https://YOUR_SCHOOL.instructure.com
canvas.accessToken=PUT_YOUR_CANVAS_TOKEN_HERE
openai.apiKey=PUT_YOUR_OPENAI_KEY_HERE
```

Supported env var overrides:
- `CANVAS_BASE_URL`
- `CANVAS_ACCESS_TOKEN`
- `OPENAI_API_KEY`

## Run
```bash
mvn spring-boot:run
```
Open: `http://localhost:8080`

## Canvas token
Canvas → Account → Settings → New Access Token.
Paste token into settings modal or config.

## API Examples
```bash
curl -X POST http://localhost:8080/api/sync
curl http://localhost:8080/api/courses
curl "http://localhost:8080/api/assignments?courseId=123"
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"What should I prioritize this week?","threadId":"default"}'
```

## Assumptions
- Uses Canvas Personal Access Token with permissions for courses/assignments/enrollments.
- Embeddings are re-generated only when chunk content hash changes.
- Settings saved in DB are plain text (masked in UI display), for local/dev convenience.
