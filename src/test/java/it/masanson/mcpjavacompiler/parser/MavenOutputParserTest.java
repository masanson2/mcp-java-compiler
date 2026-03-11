package it.masanson.mcpjavacompiler.parser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MavenOutputParserTest {

    private final MavenOutputParser parser = new MavenOutputParser();

    @Test
    void shouldExtractStructuredErrors() {
        String output = """
                [ERROR] COMPILATION ERROR :
                [ERROR] /tmp/project/src/main/java/it/acme/UserService.java:[42,17] cannot find symbol
                [ERROR] symbol:   method findByCode(java.lang.String)
                [ERROR] location: variable repository of type UserRepository
                [ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.13.0:compile
                """;

        MavenOutputParser.ParsedMavenOutput result = parser.parse(output);

        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().getFirst().getFile()).isEqualTo("/tmp/project/src/main/java/it/acme/UserService.java");
        assertThat(result.errors().getFirst().getLine()).isEqualTo(42);
        assertThat(result.errors().getFirst().getColumn()).isEqualTo(17);
        assertThat(result.errors().getFirst().getMessage()).isEqualTo("cannot find symbol");
        assertThat(result.errors().getFirst().getSymbol()).isEqualTo("method findByCode(java.lang.String)");
        assertThat(result.errors().getFirst().getLocation()).isEqualTo("variable repository of type UserRepository");
        assertThat(result.rawSummary()).isEqualTo("COMPILATION ERROR :");
    }
}
