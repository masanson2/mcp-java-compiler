package it.masanson.mcpjavacompiler;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import it.masanson.mcpjavacompiler.transport.McpStdioServer;
import jakarta.inject.Inject;

@QuarkusMain
public class McpJavaCompilerApplication implements QuarkusApplication {

    @Inject
    McpStdioServer stdioServer;

    @Override
    public int run(String... args) throws Exception {
        return stdioServer.run();
    }
}
