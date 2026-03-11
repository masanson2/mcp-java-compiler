# mcp-java-compiler

MCP server in Quarkus per validare in modo affidabile la compilazione di progetti Java Maven single-module.

## Tool esposti

- `java_project_inspect`
- `java_compile_validate`
- `java_test_compile_validate`

## Vincoli MVP

- solo Maven
- solo single-module
- solo path sotto `mcp.validation.allowed-root`
- nessun comando arbitrario
- transport MCP su `stdin/stdout` con JSON newline-delimited

## Build

```bash
mvn test
mvn -DskipTests package
```

## Esecuzione locale

```bash
java -jar target/quarkus-app/quarkus-run.jar
```

Il processo legge messaggi MCP JSON-RPC da `stdin` e scrive le risposte su `stdout`.

## Esempio rapido

```bash
printf '%s\n' \
  '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{}}' \
  '{"jsonrpc":"2.0","id":2,"method":"tools/list"}' \
  '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"java_project_inspect","arguments":{"projectPath":"/home/masanson/projects/mcp-java-compiler"}}}' \
  | java -jar target/quarkus-app/quarkus-run.jar
```

## Configurazione

`src/main/resources/application.properties`

```properties
mcp.validation.allowed-root=/home/masanson/projects
mcp.validation.default-timeout-seconds=120
mcp.validation.test-compile-timeout-seconds=180
mcp.validation.maven-command=mvn
mcp.validation.cache-enabled=false
```

## Esempio configurazione MCP

```json
{
  "mcpServers": {
    "java-compiler": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/mcp-java-compiler/target/quarkus-app/quarkus-run.jar"
      ]
    }
  }
}
```
