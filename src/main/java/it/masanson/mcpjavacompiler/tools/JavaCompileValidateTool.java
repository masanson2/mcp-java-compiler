package it.masanson.mcpjavacompiler.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.masanson.mcpjavacompiler.model.CompileResult;
import it.masanson.mcpjavacompiler.service.MavenValidationService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class JavaCompileValidateTool implements McpToolHandler {

    private final ObjectMapper objectMapper;
    private final MavenValidationService mavenValidationService;

    @Inject
    public JavaCompileValidateTool(ObjectMapper objectMapper, MavenValidationService mavenValidationService) {
        this.objectMapper = objectMapper;
        this.mavenValidationService = mavenValidationService;
    }

    @Override
    public ToolDescriptor descriptor() {
        return new ToolDescriptor(
                "java_compile_validate",
                "Run mvn compile with a safe whitelist and return structured compiler errors.",
                validationSchema());
    }

    @Override
    public ToolExecutionResult execute(ObjectNode arguments) {
        try {
            String projectPath = requiredText(arguments, "projectPath");
            Integer timeoutSeconds = optionalInteger(arguments, "timeoutSeconds");
            boolean quiet = optionalBoolean(arguments, "quiet", true);
            CompileResult result = mavenValidationService.validate(projectPath, timeoutSeconds, quiet, false);
            return ToolExecutionResult.success(summaryFor(result), objectMapper.valueToTree(result));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ToolExecutionResult.error(e.getMessage(), errorPayload(e.getMessage()));
        }
    }

    protected ObjectNode validationSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");
        ObjectNode properties = schema.putObject("properties");
        properties.putObject("projectPath")
                .put("type", "string")
                .put("description", "Absolute path of the Maven project to validate.");
        properties.putObject("timeoutSeconds")
                .put("type", "integer")
                .put("minimum", 1)
                .put("description", "Optional override for the compile timeout.");
        properties.putObject("quiet")
                .put("type", "boolean")
                .put("description", "Whether Maven should run with -q. Defaults to true.");
        ArrayNode required = schema.putArray("required");
        required.add("projectPath");
        schema.put("additionalProperties", false);
        return schema;
    }

    protected String summaryFor(CompileResult result) {
        return switch (result.getStatus()) {
            case "success" -> "Compilation succeeded in " + result.getDurationMs() + " ms.";
            case "timeout" -> "Compilation timed out after " + result.getDurationMs() + " ms.";
            default -> "Compilation failed with " + result.getErrors().size() + " structured error(s).";
        };
    }

    protected String requiredText(ObjectNode arguments, String fieldName) {
        if (arguments == null || arguments.path(fieldName).asText().isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return arguments.path(fieldName).asText();
    }

    protected Integer optionalInteger(ObjectNode arguments, String fieldName) {
        if (arguments == null || arguments.path(fieldName).isMissingNode() || arguments.path(fieldName).isNull()) {
            return null;
        }
        if (!arguments.path(fieldName).canConvertToInt()) {
            throw new IllegalArgumentException(fieldName + " must be an integer.");
        }
        return arguments.path(fieldName).asInt();
    }

    protected boolean optionalBoolean(ObjectNode arguments, String fieldName, boolean defaultValue) {
        if (arguments == null || arguments.path(fieldName).isMissingNode() || arguments.path(fieldName).isNull()) {
            return defaultValue;
        }
        if (!arguments.path(fieldName).isBoolean()) {
            throw new IllegalArgumentException(fieldName + " must be a boolean.");
        }
        return arguments.path(fieldName).asBoolean();
    }

    protected ObjectNode errorPayload(String message) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("status", "error");
        payload.put("message", message);
        return payload;
    }
}
