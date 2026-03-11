package it.masanson.mcpjavacompiler.tools;

import com.fasterxml.jackson.databind.node.ObjectNode;

public record ToolDescriptor(String name, String description, ObjectNode inputSchema) {
}
