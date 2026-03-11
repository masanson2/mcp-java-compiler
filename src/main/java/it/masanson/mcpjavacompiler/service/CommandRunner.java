package it.masanson.mcpjavacompiler.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public class CommandRunner {

    public ExecutionResult run(List<String> command, Path workingDirectory, int timeoutSeconds)
            throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(workingDirectory.toFile());
        processBuilder.redirectErrorStream(true);

        long start = System.nanoTime();
        Process process = processBuilder.start();

        try (var executor = Executors.newSingleThreadExecutor()) {
            Future<String> outputFuture = executor.submit(() -> new String(process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8));

            boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!completed) {
                process.destroy();
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    process.waitFor(5, TimeUnit.SECONDS);
                }
                String output = getOutput(outputFuture);
                return new ExecutionResult(ExecutionResult.Status.TIMEOUT, -1, elapsedMillis(start), output);
            }

            int exitCode = process.exitValue();
            String output = getOutput(outputFuture);
            ExecutionResult.Status status = exitCode == 0 ? ExecutionResult.Status.SUCCESS : ExecutionResult.Status.FAILURE;
            return new ExecutionResult(status, exitCode, elapsedMillis(start), output);
        }
    }

    private String getOutput(Future<String> outputFuture) throws InterruptedException, IOException {
        try {
            return outputFuture.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw new IOException("Cannot read process output.", e.getCause());
        } catch (TimeoutException e) {
            throw new IOException("Timed out while reading process output.", e);
        }
    }

    private long elapsedMillis(long start) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
    }
}
