#!/bin/bash

# Test script for MCP Server Command

echo "Testing MCP Server Command..."

# Test 1: Simple command
echo "Test 1: Running 'echo Hello World'"
echo '{"jsonrpc":"2.0","method":"tools/call","id":1,"params":{"name":"run_command","arguments":{"command":"echo Hello World"}}}'| java -jar build/libs/mcp-server-command-0.5.0.jar

# Test 2: Command with working directory
echo -e "\nTest 2: Running 'pwd' in /tmp"
echo '{"jsonrpc":"2.0","method":"tools/call","id":2,"params":{"name":"run_command","arguments":{"command":"pwd","workdir":"/tmp"}}}' | java -jar build/libs/mcp-server-command-0.5.0.jar

# Test 3: Command with stdin
echo -e "\nTest 3: Running 'cat' with stdin"
echo '{"jsonrpc":"2.0","method":"tools/call","id":3,"params":{"name":"run_command","arguments":{"command":"cat","stdin":"This is from stdin"}}}' | java -jar build/libs/mcp-server-command-0.5.0.jar

echo -e "\nAll tests completed."
