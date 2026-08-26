package org.example.com.aiassistantservice.tool;

public record ToolResult(String toolName, boolean success, Object payload, String error) {

    public static ToolResult success(String toolName, Object payload) {
        return new ToolResult(toolName, true, payload, null);
    }

    public static ToolResult failure(String toolName, String error) {
        return new ToolResult(toolName, false, null, error);
    }
}
