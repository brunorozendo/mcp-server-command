# MCP Command Server

A Model Context Protocol (MCP) server that enables secure command execution on the host system. This server allows AI assistants like Claude to run shell commands with optional working directory and stdin support.

## Features

- **Command Execution**: Run any shell command on the host system
- **Working Directory Support**: Execute commands in specific directories
- **STDIN Support**: Pipe input data to commands
- **Cross-Platform**: Works on macOS, Linux, and Windows
- **Timeout Protection**: Commands timeout after 60 seconds to prevent hanging
- **Special Fish Shell Support**: Enhanced handling for the Fish shell
- **Advanced Logging**: Powered by Logback with configurable log levels (ERROR, WARN, INFO, DEBUG, TRACE), optional file logging, and rolling file support

## Prerequisites

- Java 21 or higher
- Gradle (for building from source)
- Claude Desktop app (for integration)

## Installation

### Building from Source

1. Clone the repository:
```bash
git clone <repository-url>
cd mcp-server-command
```

2. Build the project:
```bash
./gradlew build
```

This will create the JAR file at `build/libs/mcp-server-command-0.5.0.jar`

### Pre-built JAR

If you have a pre-built JAR, skip the building step and proceed to configuration.

## Configuration

### Claude Desktop Integration

1. Locate your Claude Desktop configuration:
   - macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
   - Windows: `%APPDATA%\Claude\claude_desktop_config.json`
   - Linux: `~/.config/Claude/claude_desktop_config.json`

2.1 Add the MCP server configuration - Java:
```json
{
  "mcpServers": {
    "mcp-server-command": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/mcp-server-command-0.5.0.jar"
      ],
      "env":{
        "LOG_DIR":"/Users/<username>/Library/Logs/Claude",
        "LOG_LEVEL":"DEBUG"
      }
    }
  }
}
```

2.1 Add the MCP server configuration - Native:
```json
{
  "mcpServers": {
    "mcp-server-command": {
      "command": "/path/to/build/native/nativeCompile/mcp-server-command",
      "env":{
        "LOG_DIR":"/Users/<username>/Library/Logs/Claude",
        "LOG_LEVEL":"DEBUG"
      }
    }
  }
}
```




3. Restart Claude Desktop to load the new server

## Usage

Once configured, Claude can use the `run_command` tool to execute commands on your system.

### Basic Examples

**Simple command execution:**
```
Run the command: ls -la
```

**Command with working directory:**
```
Run 'git status' in the directory /Users/username/my-project
```

**Command with stdin:**
```
Create a new file called hello.txt with the content "Hello, World!" using the cat command
```

**Python script execution:**
```
Run this Python script:
print("Hello from Python")
for i in range(5):
    print(f"Count: {i}")
```

## API Reference

### Tool: run_command

Executes a command on the host system.

**Parameters:**
- `command` (string, required): The command to execute with arguments
- `workdir` (string, optional): The working directory for command execution
- `stdin` (string, optional): Text to pipe into the command's STDIN

**Returns:**
- `stdout`: Standard output from the command
- `stderr`: Standard error output from the command
- `message`: Error message if command fails
- `isError`: Boolean indicating if the command failed

**Example Request:**
```json
{
  "jsonrpc": "2.0",
  "method": "tools/call",
  "id": 1,
  "params": {
    "name": "run_command",
    "arguments": {
      "command": "echo Hello World",
      "workdir": "/tmp",
      "stdin": "Input data"
    }
  }
}
```

## Development

### Project Structure
```
mcp-server-command/
├── src/main/java/com/example/mcp/server/command/
│   ├── McpCommandServer.java    # Main server implementation
│   ├── CommandExecutor.java     # Command execution logic
│   └── CommandResult.java       # Result data structure
├── build.gradle                 # Build configuration
├── test_server.sh              # Testing script
└── claude_desktop_config_example.json
```

### Running Tests

Execute the test script to verify functionality:
```bash
./test_server.sh
```

### Building a Distribution

Create distribution archives (ZIP and TAR):
```bash
./gradlew distZip distTar
```

## Security Considerations

⚠️ **WARNING**: This server executes commands with the same privileges as the user running the Java process.

**Security Best Practices:**
1. Only install this server if you trust the AI assistant
2. Run the server with minimal necessary privileges
3. Consider using a restricted user account for the server
4. Be cautious when sharing your screen or command outputs
5. Regularly review which commands are being executed
6. Consider implementing command whitelisting for production use

**Potential Risks:**
- File system access and modification
- Network operations
- Process execution
- Access to environment variables and system information

## Troubleshooting

### Server not appearing in Claude
1. Verify the configuration file path is correct
2. Ensure the JAR file path is absolute, not relative
3. Check that Java 21+ is installed: `java -version`
4. Restart Claude Desktop after configuration changes

### Commands failing
1. Set log level to DEBUG or TRACE to see detailed error messages
2. Check file permissions for the working directory
3. Verify the command syntax is correct for your shell
4. Test commands directly in terminal first

### Timeout errors
- Commands timeout after 60 seconds
- For long-running commands, consider breaking them into smaller steps
- Use background processes with caution

## Advanced Usage

### Creating Files
```bash
# Using cat with stdin
echo "File content" | cat > newfile.txt

# Or directly with stdin parameter
cat > newfile.txt
# with stdin: "File content"
```

### Running Scripts
```bash
# Python
python3 -c "print('Hello')"

# Or with stdin for longer scripts
python3
# with stdin: <your script content>
```

### Fish Shell Support
The server includes special handling for Fish shell to properly handle stdin:
```bash
fish -c "echo $USER"
```

## Version History

- **0.5.0** - Current version with full MCP support
  - Command execution with timeout protection
  - Working directory support
  - STDIN support
  - Special Fish shell handling
  - Advanced Logback-based logging with file rotation support

### Custom Logback Configuration

For advanced users, you can provide your own `logback.xml` configuration file:
1. Create your custom `logback.xml`
2. Place it in the classpath or specify its location with `-Dlogback.configurationFile=/path/to/logback.xml`

## License

[Add your license information here]

## Contributing

[Add contribution guidelines if applicable]

## Support

For issues and questions:
- Check the troubleshooting section
- Set appropriate log level for debugging
- [Add support contact/repository issues link]
