package it.masanson.mcpjavacompiler.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import it.masanson.mcpjavacompiler.config.ValidationConfig;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PathPolicyTest {

    private final PathPolicy pathPolicy = new PathPolicy(new StubValidationConfig());

    @Test
    void shouldAcceptPathInsideAllowedRoot() {
        Path resolved = pathPolicy.resolveAndValidate("/home/masanson/projects/sample");

        assertThat(resolved).isEqualTo(Path.of("/home/masanson/projects/sample"));
    }

    @Test
    void shouldRejectPathOutsideAllowedRoot() {
        assertThatThrownBy(() -> pathPolicy.resolveAndValidate("/tmp/outside"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not allowed");
    }

    private static class StubValidationConfig implements ValidationConfig {

        @Override
        public String allowedRoot() {
            return "/home/masanson/projects";
        }

        @Override
        public int defaultTimeoutSeconds() {
            return 120;
        }

        @Override
        public int testCompileTimeoutSeconds() {
            return 180;
        }

        @Override
        public String mavenCommand() {
            return "mvn";
        }

        @Override
        public boolean cacheEnabled() {
            return false;
        }
    }
}
