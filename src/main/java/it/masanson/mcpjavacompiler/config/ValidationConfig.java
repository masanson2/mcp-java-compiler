package it.masanson.mcpjavacompiler.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "mcp.validation")
public interface ValidationConfig {

    String allowedRoot();

    @WithDefault("120")
    int defaultTimeoutSeconds();

    @WithDefault("180")
    int testCompileTimeoutSeconds();

    @WithDefault("mvn")
    String mavenCommand();

    @WithDefault("false")
    boolean cacheEnabled();
}
