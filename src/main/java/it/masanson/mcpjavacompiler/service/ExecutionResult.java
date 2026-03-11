package it.masanson.mcpjavacompiler.service;

public record ExecutionResult(Status status, int exitCode, long durationMs, String output) {

    public enum Status {
        SUCCESS,
        FAILURE,
        TIMEOUT
    }
}
