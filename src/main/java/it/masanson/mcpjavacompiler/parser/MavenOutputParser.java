package it.masanson.mcpjavacompiler.parser;

import it.masanson.mcpjavacompiler.model.CompileError;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class MavenOutputParser {

    private static final Pattern ERROR_PATTERN = Pattern
            .compile("^\\[ERROR\\]\\s+(.+\\.java):\\[(\\d+),(\\d+)]\\s+(.+)$");
    private static final Pattern SYMBOL_PATTERN = Pattern.compile("^\\[ERROR\\]\\s+symbol:\\s+(.+)$");
    private static final Pattern LOCATION_PATTERN = Pattern.compile("^\\[ERROR\\]\\s+location:\\s+(.+)$");

    public ParsedMavenOutput parse(String output) {
        List<CompileError> errors = new ArrayList<>();
        String rawSummary = null;
        CompileError currentError = null;

        for (String line : output.split("\\R")) {
            Matcher errorMatcher = ERROR_PATTERN.matcher(line);
            if (errorMatcher.matches()) {
                if (currentError != null) {
                    errors.add(currentError);
                }
                currentError = new CompileError(
                        errorMatcher.group(1).trim(),
                        Integer.parseInt(errorMatcher.group(2)),
                        Integer.parseInt(errorMatcher.group(3)),
                        errorMatcher.group(4).trim(),
                        null,
                        null);
                continue;
            }

            if (currentError != null) {
                Matcher symbolMatcher = SYMBOL_PATTERN.matcher(line);
                if (symbolMatcher.matches()) {
                    currentError.setSymbol(symbolMatcher.group(1).trim());
                    continue;
                }

                Matcher locationMatcher = LOCATION_PATTERN.matcher(line);
                if (locationMatcher.matches()) {
                    currentError.setLocation(locationMatcher.group(1).trim());
                    continue;
                }
            }

            if (line.startsWith("[ERROR]")) {
                String message = line.substring("[ERROR]".length()).trim();
                if (!message.isBlank() && !message.startsWith("symbol:") && !message.startsWith("location:")
                        && rawSummary == null) {
                    rawSummary = message;
                }
            }
        }

        if (currentError != null) {
            errors.add(currentError);
        }

        if (rawSummary == null) {
            rawSummary = errors.isEmpty() ? null : errors.getFirst().getMessage();
        }

        return new ParsedMavenOutput(errors, rawSummary);
    }

    public record ParsedMavenOutput(List<CompileError> errors, String rawSummary) {
    }
}
