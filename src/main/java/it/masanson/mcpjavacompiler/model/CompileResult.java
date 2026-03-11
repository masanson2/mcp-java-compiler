package it.masanson.mcpjavacompiler.model;

import java.util.List;

public class CompileResult {

    private String status;
    private String command;
    private int exitCode;
    private long durationMs;
    private List<CompileError> errors;
    private String rawSummary;

    public CompileResult() {
    }

    public CompileResult(String status, String command, int exitCode, long durationMs, List<CompileError> errors,
            String rawSummary) {
        this.status = status;
        this.command = command;
        this.exitCode = exitCode;
        this.durationMs = durationMs;
        this.errors = errors;
        this.rawSummary = rawSummary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public List<CompileError> getErrors() {
        return errors;
    }

    public void setErrors(List<CompileError> errors) {
        this.errors = errors;
    }

    public String getRawSummary() {
        return rawSummary;
    }

    public void setRawSummary(String rawSummary) {
        this.rawSummary = rawSummary;
    }
}
