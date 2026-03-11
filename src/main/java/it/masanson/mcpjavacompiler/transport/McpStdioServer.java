package it.masanson.mcpjavacompiler.transport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class McpStdioServer {

    private final ObjectMapper objectMapper;
    private final McpRequestDispatcher requestDispatcher;

    @Inject
    public McpStdioServer(ObjectMapper objectMapper, McpRequestDispatcher requestDispatcher) {
        this.objectMapper = objectMapper;
        this.requestDispatcher = requestDispatcher;
    }

    public int run() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                ObjectNode response = handleLine(line);
                if (response != null) {
                    writer.write(objectMapper.writeValueAsString(response));
                    writer.newLine();
                    writer.flush();
                }
            }
        }
        return 0;
    }

    private ObjectNode handleLine(String line) {
        try {
            JsonNode message = objectMapper.readTree(line);
            return requestDispatcher.dispatch(message);
        } catch (JsonProcessingException e) {
            return parseError();
        } catch (IllegalArgumentException e) {
            return invalidRequest(e.getMessage());
        }
    }

    private ObjectNode parseError() {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.putNull("id");
        ObjectNode error = response.putObject("error");
        error.put("code", -32700);
        error.put("message", "Parse error.");
        return response;
    }

    private ObjectNode invalidRequest(String message) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.putNull("id");
        ObjectNode error = response.putObject("error");
        error.put("code", -32602);
        error.put("message", message);
        return response;
    }
}
