package it.masanson.mcpjavacompiler.transport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.masanson.mcpjavacompiler.tools.ToolDescriptor;
import it.masanson.mcpjavacompiler.tools.ToolExecutionResult;
import it.masanson.mcpjavacompiler.tools.ToolRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class McpRequestDispatcher {

    private static final String PROTOCOL_VERSION = "2025-03-26";
    private static final String SERVER_NAME = "mcp-java-compiler";
    private static final String SERVER_VERSION = "0.1.0";

    private final ObjectMapper objectMapper;
    private final ToolRegistry toolRegistry;

    @Inject
    public McpRequestDispatcher(ObjectMapper objectMapper, ToolRegistry toolRegistry) {
        this.objectMapper = objectMapper;
        this.toolRegistry = toolRegistry;
    }

    public ObjectNode dispatch(JsonNode message) {
        String method = message.path("method").asText(null);
        JsonNode id = message.get("id");

        if (method == null || method.isBlank()) {
            return error(id, -32600, "Invalid request.");
        }

        return switch (method) {
            case "initialize" -> response(id, initializeResult());
            case "notifications/initialized" -> null;
            case "ping" -> response(id, objectMapper.createObjectNode());
            case "tools/list" -> response(id, listTools());
            case "tools/call" -> response(id, callTool(message.path("params")));
            default -> error(id, -32601, "Method not found: " + method);
        };
    }

    private ObjectNode initializeResult() {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("protocolVersion", PROTOCOL_VERSION);

        ObjectNode capabilities = result.putObject("capabilities");
        capabilities.putObject("tools").put("listChanged", false);

        ObjectNode serverInfo = result.putObject("serverInfo");
        serverInfo.put("name", SERVER_NAME);
        serverInfo.put("version", SERVER_VERSION);

        result.put("instructions",
                "Use java_project_inspect before compile validation. This server supports only Maven single-module projects.");
        return result;
    }

    private ObjectNode listTools() {
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode tools = result.putArray("tools");
        for (ToolDescriptor descriptor : toolRegistry.descriptors()) {
            ObjectNode toolNode = tools.addObject();
            toolNode.put("name", descriptor.name());
            toolNode.put("description", descriptor.description());
            toolNode.set("inputSchema", descriptor.inputSchema());
        }
        return result;
    }

    private ObjectNode callTool(JsonNode params) {
        if (params == null || !params.isObject()) {
            throw new IllegalArgumentException("tools/call params must be an object.");
        }

        String name = params.path("name").asText();
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tool name is required.");
        }

        JsonNode argumentsNode = params.path("arguments");
        ObjectNode arguments = argumentsNode instanceof ObjectNode objectNode ? objectNode : objectMapper.createObjectNode();

        ToolExecutionResult toolResult = toolRegistry.get(name).execute(arguments);
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode content = result.putArray("content");
        content.addObject()
                .put("type", "text")
                .put("text", toolResult.text());
        result.set("structuredContent", toolResult.structuredContent());
        result.put("isError", toolResult.isError());
        return result;
    }

    private ObjectNode response(JsonNode id, JsonNode result) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.set("id", id == null ? objectMapper.getNodeFactory().nullNode() : id);
        response.set("result", result);
        return response;
    }

    private ObjectNode error(JsonNode id, int code, String message) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.set("id", id == null ? objectMapper.getNodeFactory().nullNode() : id);
        ObjectNode error = response.putObject("error");
        error.put("code", code);
        error.put("message", message);
        return response;
    }
}
