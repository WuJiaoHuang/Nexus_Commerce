package org.example.com.aiassistantservice.controller;

import org.example.com.common.exception.ErrorCode;
import org.example.com.common.result.Result;
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
    public ResponseEntity<Result<?>> assist(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AiPromptRequest request
    ) {
        String token = jwtTokenValidator.stripBearerPrefix(authorization);
        JwtValidationResult validation = jwtTokenValidator.validate(token);
        if (!validation.valid()) {
            Result<?> result = Result.error(ErrorCode.UNAUTHORIZED.getCode(), validation.message());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(result);
        }
        AiPromptResponse response = aiAssistantService.generate(request, validation.username());
        Result<?> result = Result.success(response);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/health")
    public Result<Map<String, String>> health() {
        return Result.success(Map.of("status", "UP"));
    }
}
