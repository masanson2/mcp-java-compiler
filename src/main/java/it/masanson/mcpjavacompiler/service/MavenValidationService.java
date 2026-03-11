package it.masanson.mcpjavacompiler.service;

import it.masanson.mcpjavacompiler.config.ValidationConfig;
import it.masanson.mcpjavacompiler.model.CompileError;
import it.masanson.mcpjavacompiler.model.CompileResult;
import it.masanson.mcpjavacompiler.model.ProjectInspectionResult;
import it.masanson.mcpjavacompiler.parser.MavenOutputParser;
import it.masanson.mcpjavacompiler.security.CommandPolicy;
import it.masanson.mcpjavacompiler.security.PathPolicy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@ApplicationScoped
public class MavenValidationService {

    private final ProjectInspectionService projectInspectionService;
    private final PathPolicy pathPolicy;
    private final CommandPolicy commandPolicy;
    private final CommandRunner commandRunner;
    private final ValidationConfig config;
    private final MavenOutputParser outputParser;

    @Inject
    public MavenValidationService(ProjectInspectionService projectInspectionService, PathPolicy pathPolicy,
            CommandPolicy commandPolicy, CommandRunner commandRunner, ValidationConfig config,
            MavenOutputParser outputParser) {
        this.projectInspectionService = projectInspectionService;
        this.pathPolicy = pathPolicy;
        this.commandPolicy = commandPolicy;
        this.commandRunner = commandRunner;
        this.config = config;
        this.outputParser = outputParser;
    }

    public CompileResult validate(String projectPathValue, Integer timeoutSeconds, boolean quiet, boolean testCompile) {
        Path projectPath = pathPolicy.resolveAndValidate(projectPathValue);
        ProjectInspectionResult inspection = projectInspectionService.inspect(projectPathValue);
        if (!inspection.isHasPom()) {
            throw new IllegalArgumentException("The target directory does not contain a pom.xml file.");
        }
        if (!inspection.isSingleModule()) {
            throw new IllegalArgumentException("Only Maven single-module projects are supported in this MVP.");
        }

        int timeout = resolveTimeout(timeoutSeconds, testCompile);
        List<String> command = commandPolicy.buildCompileCommand(testCompile, quiet);

        try {
            ExecutionResult executionResult = commandRunner.run(command, projectPath, timeout);
            return switch (executionResult.status()) {
                case SUCCESS -> new CompileResult(
                        "success",
                        commandPolicy.render(command),
                        executionResult.exitCode(),
                        executionResult.durationMs(),
                        List.of(),
                        "Compilation completed successfully.");
                case FAILURE -> failureResult(command, executionResult.output(), executionResult.exitCode(),
                        executionResult.durationMs(), projectPath);
                case TIMEOUT -> timeoutResult(command, executionResult.output(), executionResult.durationMs(), timeout,
                        projectPath);
            };
        } catch (IOException e) {
            throw new IllegalStateException("Cannot execute Maven command.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Maven validation was interrupted.", e);
        }
    }

    private CompileResult failureResult(List<String> command, String output, int exitCode, long durationMs, Path projectPath) {
        MavenOutputParser.ParsedMavenOutput parsedOutput = outputParser.parse(output);
        return new CompileResult(
                "failure",
                commandPolicy.render(command),
                exitCode,
                durationMs,
                relativize(projectPath, parsedOutput.errors()),
                parsedOutput.rawSummary());
    }

    private CompileResult timeoutResult(List<String> command, String output, long durationMs, int timeoutSeconds,
            Path projectPath) {
        MavenOutputParser.ParsedMavenOutput parsedOutput = outputParser.parse(output);
        String summary = parsedOutput.rawSummary() != null
                ? parsedOutput.rawSummary()
                : "Compilation timed out after " + timeoutSeconds + " seconds.";
        return new CompileResult(
                "timeout",
                commandPolicy.render(command),
                -1,
                durationMs,
                relativize(projectPath, parsedOutput.errors()),
                summary);
    }

    private List<CompileError> relativize(Path projectPath, List<CompileError> errors) {
        return errors.stream()
                .map(error -> new CompileError(
                        relativize(projectPath, error.getFile()),
                        error.getLine(),
                        error.getColumn(),
                        error.getMessage(),
                        error.getSymbol(),
                        error.getLocation()))
                .toList();
    }

    private String relativize(Path projectPath, String file) {
        if (file == null || file.isBlank()) {
            return file;
        }
        Path filePath = Path.of(file).normalize();
        return filePath.startsWith(projectPath) ? projectPath.relativize(filePath).toString() : filePath.toString();
    }

    private int resolveTimeout(Integer timeoutSeconds, boolean testCompile) {
        int effectiveTimeout = timeoutSeconds != null
                ? timeoutSeconds
                : (testCompile ? config.testCompileTimeoutSeconds() : config.defaultTimeoutSeconds());
        if (effectiveTimeout <= 0) {
            throw new IllegalArgumentException("timeoutSeconds must be greater than zero.");
        }
        return effectiveTimeout;
    }
}
