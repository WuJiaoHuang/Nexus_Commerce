package org.example.com.aiassistantservice.impl;

import org.example.com.aiassistantservice.dto.AiPromptRequest;
import org.example.com.aiassistantservice.dto.AiPromptResponse;
import org.example.com.aiassistantservice.service.AiAssistantService;
import org.example.com.aiassistantservice.tool.CommerceToolClient;
import org.example.com.aiassistantservice.tool.ToolResult;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MockAiAssistantServiceImpl implements AiAssistantService {

    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("order(?:Id)?[:=\\s]+([A-Za-z0-9-]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("product(?:Id)?[:=\\s]+([A-Za-z0-9-]+)", Pattern.CASE_INSENSITIVE);

    private final CommerceToolClient commerceToolClient;

    public MockAiAssistantServiceImpl(CommerceToolClient commerceToolClient) {
        this.commerceToolClient = commerceToolClient;
    }

    @Override
    public AiPromptResponse generate(AiPromptRequest request, String username) {
        String useCase = request.useCase() == null ? "general" : request.useCase();
        String prompt = request.prompt() == null ? "" : request.prompt().trim();

        if ("commerce-support".equalsIgnoreCase(useCase) || prompt.contains("订单") || prompt.toLowerCase().contains("order")) {
            return answerWithCommerceTools(useCase, prompt, username);
        }

        String answer = switch (useCase.toLowerCase()) {
            case "order-summary" -> "Order summary generated for user %s. Prompt: %s".formatted(username, prompt);
            case "inventory-risk" -> "Inventory risk analysis generated for user %s. Prompt: %s".formatted(username, prompt);
            case "support-draft" -> "Support draft generated for user %s. Prompt: %s".formatted(username, prompt);
            default -> "Generic assistant response for user %s. Prompt: %s".formatted(username, prompt);
        };

        return new AiPromptResponse(useCase, answer, "mock-bedrock-adapter");
    }

    private AiPromptResponse answerWithCommerceTools(String useCase, String prompt, String username) {
        Map<String, Object> toolResults = new LinkedHashMap<>();
        Optional<String> orderId = extract(ORDER_ID_PATTERN, prompt);
        Optional<String> productId = extract(PRODUCT_ID_PATTERN, prompt);

        orderId.ifPresent(id -> put(toolResults, commerceToolClient.queryOrder(id)));
        productId.ifPresent(id -> {
            put(toolResults, commerceToolClient.queryProduct(id));
            put(toolResults, commerceToolClient.queryInventory(id));
        });

        if (prompt.contains("售后") || prompt.toLowerCase().contains("refund") || prompt.toLowerCase().contains("cancel")) {
            put(toolResults, commerceToolClient.queryAfterSalesRule(prompt));
        }

        String answer;
        if (toolResults.isEmpty()) {
            answer = "I can help user %s check order, product, inventory, or after-sales status. Please include orderId or productId."
                    .formatted(username);
        } else {
            answer = "Commerce support answer for user %s. I called %d backend tool(s) and summarized the latest service state."
                    .formatted(username, toolResults.size());
        }
        return new AiPromptResponse(useCase, answer, "mock-tool-calling-agent", toolResults);
    }

    private Optional<String> extract(Pattern pattern, String prompt) {
        Matcher matcher = pattern.matcher(prompt);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }

    private void put(Map<String, Object> toolResults, ToolResult result) {
        toolResults.put(result.toolName(), result.success() ? result.payload() : Map.of("error", result.error()));
    }
}
