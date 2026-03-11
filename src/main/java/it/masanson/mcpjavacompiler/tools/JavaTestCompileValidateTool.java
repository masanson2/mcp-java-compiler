package it.masanson.mcpjavacompiler.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.masanson.mcpjavacompiler.model.CompileResult;
import it.masanson.mcpjavacompiler.service.MavenValidationService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class JavaTestCompileValidateTool extends JavaCompileValidateTool {

    private final ObjectMapper objectMapper;
    private final MavenValidationService mavenValidationService;

    @Inject
    public JavaTestCompileValidateTool(ObjectMapper objectMapper, MavenValidationService mavenValidationService) {
        super(objectMapper, mavenValidationService);
        this.objectMapper = objectMapper;
        this.mavenValidationService = mavenValidationService;
    }

    @Override
    public ToolDescriptor descriptor() {
        return new ToolDescriptor(
                "java_test_compile_validate",
                "Run mvn test-compile with a safe whitelist and return structured compiler errors.",
                super.descriptor().inputSchema());
    }

    @Override
    public ToolExecutionResult execute(ObjectNode arguments) {
        try {
            String projectPath = requiredText(arguments, "projectPath");
            Integer timeoutSeconds = optionalInteger(arguments, "timeoutSeconds");
            boolean quiet = optionalBoolean(arguments, "quiet", true);
            CompileResult result = mavenValidationService.validate(projectPath, timeoutSeconds, quiet, true);
            return ToolExecutionResult.success(summaryFor(result), objectMapper.valueToTree(result));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ToolExecutionResult.error(e.getMessage(), errorPayload(e.getMessage()));
        }
    }
}
