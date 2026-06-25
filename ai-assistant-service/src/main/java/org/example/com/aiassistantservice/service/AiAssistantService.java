package org.example.com.aiassistantservice.service;

import org.example.com.aiassistantservice.dto.AiPromptRequest;
import org.example.com.aiassistantservice.dto.AiPromptResponse;

public interface AiAssistantService {
    AiPromptResponse generate(AiPromptRequest request, String username);
}
