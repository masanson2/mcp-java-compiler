package it.masanson.mcpjavacompiler.tools;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface McpToolHandler {

    ToolDescriptor descriptor();

    ToolExecutionResult execute(ObjectNode arguments);
}
