package it.masanson.mcpjavacompiler.security;

import it.masanson.mcpjavacompiler.config.ValidationConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CommandPolicy {

    private final ValidationConfig config;

    @Inject
    public CommandPolicy(ValidationConfig config) {
        this.config = config;
    }

    public List<String> buildCompileCommand(boolean testCompile, boolean quiet) {
        List<String> command = new ArrayList<>();
        command.add(config.mavenCommand());
        if (quiet) {
            command.add("-q");
        }
        command.add("-DskipTests");
        command.add(testCompile ? "test-compile" : "compile");
        return List.copyOf(command);
    }

    public String render(List<String> command) {
        return String.join(" ", command);
    }
}
