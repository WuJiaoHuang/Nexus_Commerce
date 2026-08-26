package org.example.com.aiassistantservice.dto;

import java.util.Map;

public record AiPromptResponse(String useCase, String answer, String modelProvider, Map<String, Object> toolResults) {
    public AiPromptResponse(String useCase, String answer, String modelProvider) {
        this(useCase, answer, modelProvider, Map.of());
    }
}
