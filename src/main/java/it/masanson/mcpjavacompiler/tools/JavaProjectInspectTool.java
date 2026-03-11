package it.masanson.mcpjavacompiler.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.masanson.mcpjavacompiler.model.ProjectInspectionResult;
import it.masanson.mcpjavacompiler.service.ProjectInspectionService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class JavaProjectInspectTool implements McpToolHandler {

    private final ObjectMapper objectMapper;
    private final ProjectInspectionService projectInspectionService;

    @Inject
    public JavaProjectInspectTool(ObjectMapper objectMapper, ProjectInspectionService projectInspectionService) {
        this.objectMapper = objectMapper;
        this.projectInspectionService = projectInspectionService;
    }

    @Override
    public ToolDescriptor descriptor() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");
        ObjectNode properties = schema.putObject("properties");
        properties.putObject("projectPath")
                .put("type", "string")
                .put("description", "Absolute path of the Maven project to inspect.");
        ArrayNode required = schema.putArray("required");
        required.add("projectPath");
        schema.put("additionalProperties", false);
        return new ToolDescriptor(
                "java_project_inspect",
                "Inspect a Maven Java project and detect whether it is single-module.",
                schema);
    }

    @Override
    public ToolExecutionResult execute(ObjectNode arguments) {
        try {
            String projectPath = requiredText(arguments, "projectPath");
            ProjectInspectionResult result = projectInspectionService.inspect(projectPath);
            String summary = result.isHasPom()
                    ? (result.isSingleModule() ? "Maven single-module project detected." : "Maven multi-module project detected.")
                    : "No pom.xml found in the target directory.";
            return ToolExecutionResult.success(summary, objectMapper.valueToTree(result));
        } catch (IllegalArgumentException e) {
            return ToolExecutionResult.error(e.getMessage(), errorPayload(e.getMessage()));
        }
    }

    private String requiredText(ObjectNode arguments, String fieldName) {
        if (arguments == null || arguments.path(fieldName).asText().isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return arguments.path(fieldName).asText();
    }

    private ObjectNode errorPayload(String message) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("status", "error");
        payload.put("message", message);
        return payload;
    }
}
