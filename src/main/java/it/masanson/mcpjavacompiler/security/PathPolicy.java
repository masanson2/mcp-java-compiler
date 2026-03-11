package it.masanson.mcpjavacompiler.security;

import it.masanson.mcpjavacompiler.config.ValidationConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;

@ApplicationScoped
public class PathPolicy {

    private final Path allowedRoot;

    @Inject
    public PathPolicy(ValidationConfig config) {
        this.allowedRoot = normalize(Paths.get(config.allowedRoot()));
    }

    public Path resolveAndValidate(String pathValue) {
        if (pathValue == null || pathValue.isBlank()) {
            throw new IllegalArgumentException("projectPath is required.");
        }

        Path projectPath = normalize(Paths.get(pathValue));
        if (!projectPath.startsWith(allowedRoot)) {
            throw new IllegalArgumentException("Path is not allowed by policy: " + projectPath);
        }
        return projectPath;
    }

    private Path normalize(Path path) {
        return path.toAbsolutePath().normalize();
    }
}
