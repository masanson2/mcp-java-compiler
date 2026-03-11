package it.masanson.mcpjavacompiler.tools;

import com.fasterxml.jackson.databind.JsonNode;

public record ToolExecutionResult(String text, JsonNode structuredContent, boolean isError) {

    public static ToolExecutionResult success(String text, JsonNode structuredContent) {
        return new ToolExecutionResult(text, structuredContent, false);
    }

    public static ToolExecutionResult error(String text, JsonNode structuredContent) {
        return new ToolExecutionResult(text, structuredContent, true);
    }
}
