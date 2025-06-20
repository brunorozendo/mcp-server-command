# MCP Command Server - API Documentation

## Technical Overview

The MCP Command Server implements the Model Context Protocol (MCP) specification to provide command execution capabilities to AI assistants. It uses the MCP SDK for Java and communicates via JSON-RPC 2.0 over standard I/O.

## Architecture

### Core Components

#### McpCommandServer
The main server class that:
- Initializes the MCP server with stdio transport
- Registers the `run_command` tool
- Handles incoming tool requests
- Manages server lifecycle

#### CommandExecutor
Responsible for:
- Process creation and management
- Command parsing and execution
- Output stream handling
- Timeout enforcement
- Special shell handling (e.g., Fish shell)

#### CommandResult
Data structure containing:
- `stdout`: Standard output content
- `stderr`: Standard error content
- `message`: Optional status/error message
- `isError`: Boolean execution status

## Protocol Details

### Server Information
```json
{
  "name": "mcp-server-command",
  "version": "0.5.0",
  "instructions": "Run commands on this [OS_NAME] machine"
}
```

### Tool Registration

The server registers a single tool:

```json
{
  "name": "run_command",
  "description": "Run a command on this [OS_NAME] machine",
  "inputSchema": {
    "type": "object",
    "properties": {
      "command": {
        "type": "string",
        "description": "Command with args"
      },
      "workdir": {
        "type": "string",
        "description": "Optional, current working directory"
      },
      "stdin": {
        "type": "string",
        "description": "Optional, text to pipe into the command's STDIN. For example, pass a python script to python3. Or, pass text for a new file to the cat command to create it!"
      }
    },
    "required": ["command"]
  }
}
```

## Implementation Details

### Command Execution Flow

1. **Request Reception**: Server receives tool call via JSON-RPC
2. **Validation**: Command parameter is validated
3. **Process Creation**: 
   - ProcessBuilder configured with command
   - Working directory set if provided
   - Process started
4. **STDIN Handling**: If provided, stdin is written to process
5. **Output Collection**: Stdout/stderr read in separate threads
6. **Timeout Management**: Process killed if exceeds 60 seconds
7. **Result Formation**: Output packaged into CallToolResult

### Shell Handling

#### Default Behavior
- Windows: Commands run via `cmd.exe /c`
- Unix/Linux/macOS: Commands run via `/bin/sh -c`

#### Fish Shell Special Case
When command starts with "fish" and stdin is provided:
```java
// Base64 encode stdin to avoid shell interpretation issues
String base64Stdin = Base64.encode(stdin);
String wrapped = command + " -c \"echo " + base64Stdin + " | base64 -d | fish\"";
// Execute via /bin/sh
```

### Error Handling

The server handles various error conditions:

| Error Type | Handling | Response |
|------------|----------|----------|
| Missing command | Validation error | `isError: true` with message |
| Timeout (>60s) | Process killed | RuntimeException |
| Process failure | Exit code ≠ 0 | `isError: true` with exit code |
| I/O errors | Logged and propagated | Exception message |

## JSON-RPC Communication

### Request Format
```json
{
  "jsonrpc": "2.0",
  "method": "tools/call",
  "id": "unique-id",
  "params": {
    "name": "run_command",
    "arguments": {
      "command": "ls -la",
      "workdir": "/home/user",
      "stdin": "optional input"
    }
  }
}
```

### Response Format

#### Success Response
```json
{
  "jsonrpc": "2.0",
  "id": "unique-id",
  "result": {
    "content": [
      {
        "type": "text",
        "text": "command output here"
      }
    ],
    "isError": false
  }
}
```

#### Error Response
```json
{
  "jsonrpc": "2.0",
  "id": "unique-id",
  "result": {
    "content": [
      {
        "type": "text",
        "text": "Error message"
      }
    ],
    "isError": true
  }
}
```

## Thread Safety

The server implementation includes:
- Separate threads for stdout/stderr reading to prevent blocking
- Thread join with timeout for cleanup
- Proper resource management with try-with-resources

## Logging

Uses SLF4J with different log levels:
- **INFO**: Server lifecycle events
- **DEBUG**: Command execution details
- **WARN**: Recoverable errors
- **ERROR**: Critical failures

Verbose mode enables additional INFO level logging for tool requests.

## Performance Characteristics

- **Timeout**: 60 seconds per command
- **Memory**: Buffers entire stdout/stderr in memory
- **Concurrency**: Single-threaded request handling
- **I/O**: Blocking I/O with thread-based reading

## Security Model

The server executes with the Java process user's privileges:
- No sandboxing or privilege dropping
- No command filtering or validation
- Full access to file system and network
- Environment variables inherited from parent process

## Integration with MCP SDK

The server uses MCP SDK 0.10.0 with:
- `McpSyncServer`: Synchronous server implementation
- `StdioServerTransportProvider`: Standard I/O transport
- `Tool`: Tool definition with JSON schema
- `CallToolResult`: Structured response format

## Extending the Server

To add new tools:

1. Define tool in `createNewTool()` method:
```java
private Tool createNewTool() {
    String schema = "{ ... }";
    return new Tool("tool_name", "description", schema);
}
```

2. Register handler in server builder:
```java
.tool(createNewTool(), this::handleNewTool)
```

3. Implement handler method:
```java
private CallToolResult handleNewTool(McpSyncServerExchange exchange, 
                                    Map<String, Object> args) {
    // Implementation
}
```

## Debugging

Enable verbose logging:
```bash
java -jar mcp-server-command.jar --verbose
```

Test with direct JSON-RPC:
```bash
echo '{"jsonrpc":"2.0","method":"tools/call","id":1,"params":{...}}' | \
  java -jar mcp-server-command.jar
```

## Limitations

1. Single command execution (no command chaining via && or ||)
2. No background process management
3. 60-second timeout for all commands
4. Memory-bound output buffering
5. No streaming output support
6. Limited to system shell interpretation

## Future Enhancements

Potential improvements:
- Command whitelisting/blacklisting
- Configurable timeout
- Streaming output support
- Background process management
- Resource usage limits
- Audit logging
- Multiple tool support
