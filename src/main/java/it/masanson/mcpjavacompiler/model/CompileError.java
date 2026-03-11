package it.masanson.mcpjavacompiler.model;

public class CompileError {

    private String file;
    private Integer line;
    private Integer column;
    private String message;
    private String symbol;
    private String location;

    public CompileError() {
    }

    public CompileError(String file, Integer line, Integer column, String message, String symbol, String location) {
        this.file = file;
        this.line = line;
        this.column = column;
        this.message = message;
        this.symbol = symbol;
        this.location = location;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }

    public Integer getColumn() {
        return column;
    }

    public void setColumn(Integer column) {
        this.column = column;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
