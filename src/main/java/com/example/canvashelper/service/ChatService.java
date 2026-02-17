package com.example.canvashelper.service;

import com.example.canvashelper.client.OpenAiClient;
import com.example.canvashelper.dto.ApiDtos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {
    private final RagService ragService;
    private final OpenAiClient openAiClient;
    @Value("${app.topK:10}")
    private int topK;

    public ChatService(RagService ragService, OpenAiClient openAiClient) {
        this.ragService = ragService;
        this.openAiClient = openAiClient;
    }

    public ApiDtos.ChatResponse answer(String message) {
        List<RagService.Retrieval> retrieved = ragService.retrieve(message, topK);
        String context = retrieved.stream().map(r -> "- " + r.chunk().getTitle() + ": " + r.chunk().getContent())
                .reduce("", (a, b) -> a + "\n" + b);

        String system = "You are a Canvas LMS study assistant. Use only provided context when factual. " +
                "If details are missing, explicitly say what is missing and suggest next steps. " +
                "Always provide actionable recommendations." + "\nContext:" + context;
        String answer = openAiClient.chat(system, message);
        return new ApiDtos.ChatResponse(answer, retrieved.stream().map(r -> r.chunk().getTitle()).toList());
    }
}
