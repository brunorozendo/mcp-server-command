# MCP Command Server - Quick Start Guide

Get up and running with MCP Command Server in 5 minutes!

## Prerequisites Checklist

- [ ] Java 21+ installed (`java -version`)
- [ ] Claude Desktop app installed
- [ ] Basic command line knowledge

## Installation Steps

### Step 1: Download the Server

#### Option A: Download Pre-built JAR
```bash
# Download the latest release JAR
wget https://github.com/YOUR_REPO/releases/download/v0.5.0/mcp-server-command-0.5.0.jar
```

#### Option B: Build from Source
```bash
# Clone and build
git clone https://github.com/YOUR_REPO/mcp-server-command.git
cd mcp-server-command
./gradlew build
# JAR will be at build/libs/mcp-server-command-0.5.0.jar
```

### Step 2: Configure Claude Desktop

1. Find your config file:
   - **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
   - **Windows**: `%APPDATA%\Claude\claude_desktop_config.json`
   - **Linux**: `~/.config/Claude/claude_desktop_config.json`

2. Edit the config file:
```json
{
  "mcpServers": {
    "mcp-server-command": {
      "command": "java",
      "args": [
        "-jar",
        "/full/path/to/mcp-server-command-0.5.0.jar"
      ]
    }
  }
}
```

⚠️ **Important**: Use the full absolute path to the JAR file!

### Step 3: Restart Claude Desktop

Completely quit and restart Claude Desktop to load the new server.

## Verify Installation

Ask Claude to run a simple command:
> "Can you run the command 'echo Hello from MCP' for me?"

If successful, you should see the output: `Hello from MCP`

## Common First Commands

### List Files
> "Show me the files in my home directory"

### Check System Info
> "What operating system am I running? Run the appropriate command to check"

### Create a File
> "Create a file called test.txt with the content 'MCP is working!'"

### Run Python
> "Run a Python script that prints the current date and time"

## Quick Examples

### File Operations
```
# Create a file
"Create a file called notes.txt with some sample content"

# Read a file
"Show me the contents of notes.txt"

# List directory
"List all files in the current directory with details"
```

### Development Tasks
```
# Git status
"Check the git status of my current project"

# Run tests
"Run 'npm test' in my project directory"

# Build project
"Build my Java project with gradle"
```

### System Tasks
```
# Check disk space
"How much disk space do I have available?"

# Check running processes
"Show me what processes are using the most CPU"

# Network info
"What's my current IP address?"
```

## Troubleshooting Quick Fixes

### Claude doesn't see the command tool
1. Check the config file path is correct
2. Ensure you used absolute paths
3. Restart Claude Desktop completely
4. Check Java is installed: `java -version`

### Commands fail immediately
1. Test the command in your terminal first
2. Check file/directory permissions
3. Try with simpler commands first

### Getting "command not found"
1. Use full paths for executables
2. Check your PATH environment variable
3. Try using shell built-ins first (echo, cd, pwd)

## Safety Tips

⚠️ **Remember**: Claude can execute ANY command you can run!

- Start with read-only commands (ls, cat, echo)
- Be careful with delete operations
- Avoid running commands with sudo/admin privileges
- Test commands in terminal first if unsure

## Next Steps

1. Read the full [README](../README.md) for detailed documentation
2. Check [API Documentation](API.md) for advanced usage
3. See [Examples](EXAMPLES.md) for more use cases
4. Review [Security](./SECURITY.md) best practices

## Quick Reference Card

| Task | Command Example |
|------|----------------|
| List files | `ls -la` |
| Show file content | `cat filename.txt` |
| Create file | `echo "content" > file.txt` |
| Run Python | `python3 -c "print('Hello')"` |
| Check directory | `pwd` |
| Change directory | `cd /path/to/dir` |
| Find files | `find . -name "*.txt"` |
| Check processes | `ps aux` (Unix) or `tasklist` (Windows) |

## Getting Help

- Enable verbose mode for debugging: Add `"--verbose"` to args
- Check the test script: `./test_server.sh`
- Review logs in Claude Desktop developer console
- File issues on GitHub

---

🎉 **You're ready to go!** Start using MCP Command Server to enhance your Claude experience!
