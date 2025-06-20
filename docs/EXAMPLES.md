# MCP Command Server - Examples

This document provides comprehensive examples of using the MCP Command Server with Claude.

## Table of Contents

1. [Basic Commands](#basic-commands)
2. [File Operations](#file-operations)
3. [Development Tasks](#development-tasks)
4. [System Administration](#system-administration)
5. [Script Execution](#script-execution)
6. [Working with Data](#working-with-data)
7. [Network Operations](#network-operations)
8. [Advanced Examples](#advanced-examples)

## Basic Commands

### Hello World
```
User: Run the command "echo Hello, World!"
Claude: I'll run that echo command for you.
Output: Hello, World!
```

### Getting Current Directory
```
User: What directory am I currently in?
Claude: I'll check your current directory using the pwd command.
Output: /Users/username/Documents
```

### Listing Files
```
User: Show me all files in the current directory
Claude: I'll list all files with details using ls -la.
Output: [detailed file listing]
```

## File Operations

### Creating a File
```
User: Create a file called notes.txt with the content "My first note"
Claude: I'll create that file using the cat command with stdin.
Command: cat > notes.txt
Stdin: My first note
```

### Reading a File
```
User: Show me the contents of notes.txt
Claude: I'll display the contents of notes.txt.
Command: cat notes.txt
Output: My first note
```

### Appending to a File
```
User: Add "Second line" to notes.txt
Claude: I'll append that text to notes.txt.
Command: echo "Second line" >> notes.txt
```

### Creating Multiple Files
```
User: Create three files: file1.txt, file2.txt, and file3.txt
Claude: I'll create those three files for you.
Command: touch file1.txt file2.txt file3.txt
```

### Finding Files
```
User: Find all .txt files in my home directory
Claude: I'll search for all .txt files in your home directory.
Command: find ~ -name "*.txt" -type f
```

## Development Tasks

### Git Operations
```
User: Check the git status of my project
Claude: I'll check the git status for you.
Command: git status
Working directory: /path/to/project
```

### Running Tests
```
User: Run the npm tests in my React project
Claude: I'll run the npm tests for you.
Command: npm test
Working directory: /path/to/react-project
```

### Building Projects
```
User: Build my Java project with Gradle
Claude: I'll build your Java project using Gradle.
Command: ./gradlew build
Working directory: /path/to/java-project
```

### Python Development
```
User: Run this Python script:
def factorial(n):
    if n <= 1:
        return 1
    return n * factorial(n-1)

print(f"Factorial of 5 is: {factorial(5)}")

Claude: I'll run that Python script for you.
Command: python3
Stdin: [the script above]
Output: Factorial of 5 is: 120
```

### Node.js Script
```
User: Run a Node.js script that shows the current date
Claude: I'll create and run a Node.js script for that.
Command: node -e "console.log(new Date().toLocaleString())"
```

## System Administration

### Checking Disk Space
```
User: How much disk space do I have?
Claude: I'll check your disk space usage.
Command: df -h
```

### Process Management
```
User: Show me all running Python processes
Claude: I'll find all Python processes for you.
Command: ps aux | grep python
```

### System Information
```
User: What's my system information?
Claude: I'll gather your system information.
Command: uname -a
```

### Memory Usage
```
User: Show me memory usage
Claude: I'll display your memory usage.
Command: free -h  # Linux
Command: vm_stat  # macOS
```

### Network Configuration
```
User: What's my IP address?
Claude: I'll check your IP address.
Command: ifconfig  # or ip addr on Linux
```

## Script Execution

### Bash Script
```
User: Create and run a bash script that counts from 1 to 5
Claude: I'll create and run that bash script.
Command: bash
Stdin: 
#!/bin/bash
for i in {1..5}; do
    echo "Count: $i"
done
```

### Python Data Processing
```
User: Process this CSV data and show the sum:
Name,Value
Item1,10
Item2,20
Item3,30

Claude: I'll process that CSV data with Python.
Command: python3
Stdin:
import csv
import io

data = """Name,Value
Item1,10
Item2,20
Item3,30"""

reader = csv.DictReader(io.StringIO(data))
total = sum(int(row['Value']) for row in reader)
print(f"Total sum: {total}")

Output: Total sum: 60
```

### Complex Pipeline
```
User: Find all .log files modified in the last 24 hours and count lines
Claude: I'll find recent log files and count their lines.
Command: find . -name "*.log" -mtime -1 -exec wc -l {} +
```

## Working with Data

### JSON Processing
```
User: Parse this JSON and extract the names:
{"users": [{"name": "Alice"}, {"name": "Bob"}]}

Claude: I'll parse that JSON data.
Command: python3 -c "import json; data='{\"users\": [{\"name\": \"Alice\"}, {\"name\": \"Bob\"}]}'; parsed=json.loads(data); print([u['name'] for u in parsed['users']])"
```

### Text Processing
```
User: Count word frequency in a file
Claude: I'll count word frequency in the file.
Command: cat file.txt | tr -s ' ' '\n' | sort | uniq -c | sort -nr
```

### Data Analysis
```
User: Calculate the average of these numbers: 10, 20, 30, 40, 50
Claude: I'll calculate the average for you.
Command: python3 -c "numbers = [10, 20, 30, 40, 50]; print(f'Average: {sum(numbers)/len(numbers)}')"
Output: Average: 30.0
```

## Network Operations

### Checking Connectivity
```
User: Check if google.com is reachable
Claude: I'll check connectivity to google.com.
Command: ping -c 3 google.com
```

### Download a File
```
User: Download a file from a URL
Claude: I'll download that file for you.
Command: curl -O https://example.com/file.txt
```

### API Request
```
User: Make a GET request to a REST API
Claude: I'll make that API request.
Command: curl -s https://api.github.com/users/github
```

## Advanced Examples

### Creating a Project Structure
```
User: Create a basic Python project structure
Claude: I'll create a Python project structure for you.
Commands:
mkdir -p my_project/{src,tests,docs}
touch my_project/README.md
touch my_project/requirements.txt
touch my_project/src/__init__.py
touch my_project/tests/__init__.py
```

### Batch File Processing
```
User: Rename all .txt files to .bak
Claude: I'll rename all .txt files to .bak.
Command: for f in *.txt; do mv "$f" "${f%.txt}.bak"; done
```

### System Monitoring Script
```
User: Create a script that monitors CPU usage every 5 seconds
Claude: I'll create a CPU monitoring script.
Command: bash
Stdin:
#!/bin/bash
while true; do
    echo "CPU Usage at $(date):"
    top -l 1 | grep "CPU usage" | head -1
    sleep 5
done
```

### Database Backup
```
User: Create a backup of my SQLite database
Claude: I'll create a backup of your SQLite database.
Command: sqlite3 mydatabase.db ".backup backup_$(date +%Y%m%d_%H%M%S).db"
```

### Log Analysis
```
User: Find the top 10 most frequent IP addresses in access.log
Claude: I'll analyze the access log for top IP addresses.
Command: awk '{print $1}' access.log | sort | uniq -c | sort -nr | head -10
```

## Working Directory Examples

### Project-Specific Commands
```
User: In my project folder /Users/me/project, run the build command
Claude: I'll run the build command in your project folder.
Command: npm run build
Working directory: /Users/me/project
```

### Multiple Directory Operations
```
User: Check git status in both my frontend and backend folders
Claude: I'll check git status in both directories.

First, checking frontend:
Command: git status
Working directory: /path/to/frontend

Then, checking backend:
Command: git status
Working directory: /path/to/backend
```

## Error Handling Examples

### Command Not Found
```
User: Run a command that doesn't exist
Claude: I'll try to run that command.
Command: nonexistentcommand
Error: Command failed with exit code: 127
Stderr: bash: nonexistentcommand: command not found
```

### Permission Denied
```
User: Try to write to a protected directory
Claude: I'll attempt to write to that directory.
Command: echo "test" > /root/file.txt
Error: Command failed with exit code: 1
Stderr: bash: /root/file.txt: Permission denied
```

## Tips for Effective Use

1. **Be Specific**: The more specific your request, the better the command
2. **Provide Context**: Mention file paths and working directories when relevant
3. **Chain Commands**: You can ask Claude to run multiple related commands
4. **Use Scripts**: For complex tasks, have Claude create and run scripts
5. **Check First**: For destructive operations, ask Claude to list files first

## Security Reminders

- Never run commands with sudo unless absolutely necessary
- Be cautious with rm commands
- Don't execute untrusted scripts
- Review commands before letting Claude execute them
- Keep sensitive data out of command outputs

---

These examples demonstrate the versatility of the MCP Command Server. You can combine these patterns to accomplish complex tasks through natural conversation with Claude!
