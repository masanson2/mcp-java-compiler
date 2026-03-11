package it.masanson.mcpjavacompiler.service;

import static org.assertj.core.api.Assertions.assertThat;

import it.masanson.mcpjavacompiler.config.ValidationConfig;
import it.masanson.mcpjavacompiler.model.ProjectInspectionResult;
import it.masanson.mcpjavacompiler.security.CommandPolicy;
import it.masanson.mcpjavacompiler.security.PathPolicy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectInspectionServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldDetectSingleModulePom() throws IOException {
        Path projectPath = tempDir.resolve("single-module");
        Files.createDirectories(projectPath);
        Files.writeString(projectPath.resolve("pom.xml"), """
                <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>it.test</groupId>
                  <artifactId>demo</artifactId>
                </project>
                """);

        ProjectInspectionService service = new ProjectInspectionService(
                new PathPolicy(new TempValidationConfig(tempDir)),
                new CommandPolicy(new TempValidationConfig(tempDir)));

        ProjectInspectionResult result = service.inspect(projectPath.toString());

        assertThat(result.isHasPom()).isTrue();
        assertThat(result.isSingleModule()).isTrue();
        assertThat(result.getPackaging()).isEqualTo("jar");
        assertThat(result.getRecommendedCompileCommand()).isEqualTo("mvn -DskipTests compile");
    }

    @Test
    void shouldDetectMultiModulePom() throws IOException {
        Path projectPath = tempDir.resolve("multi-module");
        Files.createDirectories(projectPath);
        Files.writeString(projectPath.resolve("pom.xml"), """
                <project>
                  <modelVersion>4.0.0</modelVersion>
                  <packaging>pom</packaging>
                  <modules>
                    <module>a</module>
                    <module>b</module>
                  </modules>
                </project>
                """);

        ProjectInspectionService service = new ProjectInspectionService(
                new PathPolicy(new TempValidationConfig(tempDir)),
                new CommandPolicy(new TempValidationConfig(tempDir)));

        ProjectInspectionResult result = service.inspect(projectPath.toString());

        assertThat(result.isHasPom()).isTrue();
        assertThat(result.isSingleModule()).isFalse();
        assertThat(result.getModules()).containsExactly("a", "b");
        assertThat(result.getPackaging()).isEqualTo("pom");
    }

    private record TempValidationConfig(Path rootPath) implements ValidationConfig {

        @Override
        public String allowedRoot() {
            return rootPath.toString();
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
