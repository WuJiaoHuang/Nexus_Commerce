package org.example.com.aiassistantservice.impl;

import org.example.com.aiassistantservice.dto.AiPromptRequest;
import org.example.com.aiassistantservice.dto.AiPromptResponse;
import org.example.com.aiassistantservice.service.AiAssistantService;
import org.springframework.stereotype.Service;

@Service
public class MockAiAssistantServiceImpl implements AiAssistantService {

    @Override
    public AiPromptResponse generate(AiPromptRequest request, String username) {
        String useCase = request.useCase() == null ? "general" : request.useCase();
        String prompt = request.prompt() == null ? "" : request.prompt().trim();

        String answer = switch (useCase.toLowerCase()) {
            case "order-summary" -> "Order summary generated for user %s. Prompt: %s".formatted(username, prompt);
            case "inventory-risk" -> "Inventory risk analysis generated for user %s. Prompt: %s".formatted(username, prompt);
            case "support-draft" -> "Support draft generated for user %s. Prompt: %s".formatted(username, prompt);
            default -> "Generic assistant response for user %s. Prompt: %s".formatted(username, prompt);
        };

        return new AiPromptResponse(useCase, answer, "mock-bedrock-adapter");
    }
}
