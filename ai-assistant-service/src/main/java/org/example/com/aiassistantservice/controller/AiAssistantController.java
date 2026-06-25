package org.example.com.aiassistantservice.controller;

import org.example.com.aiassistantservice.dto.AiPromptRequest;
import org.example.com.aiassistantservice.dto.AiPromptResponse;
import org.example.com.aiassistantservice.service.AiAssistantService;
import org.example.com.securityplatform.jwt.JwtTokenValidator;
import org.example.com.securityplatform.jwt.JwtValidationResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;
    private final JwtTokenValidator jwtTokenValidator;

    public AiAssistantController(AiAssistantService aiAssistantService, JwtTokenValidator jwtTokenValidator) {
        this.aiAssistantService = aiAssistantService;
        this.jwtTokenValidator = jwtTokenValidator;
    }

    @PostMapping("/assist")
    public ResponseEntity<?> assist(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AiPromptRequest request
    ) {
        String token = jwtTokenValidator.stripBearerPrefix(authorization);
        JwtValidationResult validation = jwtTokenValidator.validate(token);
        if (!validation.valid()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", validation.message()));
        }
        AiPromptResponse response = aiAssistantService.generate(request, validation.username());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
