package it.masanson.mcpjavacompiler.tools;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class ToolRegistry {

    private final Map<String, McpToolHandler> handlersByName;
    private final List<ToolDescriptor> descriptors;

    @Inject
    public ToolRegistry(Instance<McpToolHandler> handlers) {
        List<McpToolHandler> handlerList = handlers.stream()
                .sorted(Comparator.comparing(handler -> handler.descriptor().name()))
                .toList();
        this.handlersByName = handlerList.stream()
                .collect(Collectors.toUnmodifiableMap(handler -> handler.descriptor().name(), Function.identity()));
        this.descriptors = handlerList.stream()
                .map(McpToolHandler::descriptor)
                .toList();
    }

    public List<ToolDescriptor> descriptors() {
        return descriptors;
    }

    public McpToolHandler get(String name) {
        McpToolHandler handler = handlersByName.get(name);
        if (handler == null) {
            throw new IllegalArgumentException("Unknown tool: " + name);
        }
        return handler;
    }
}
