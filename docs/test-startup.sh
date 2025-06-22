#!/bin/bash

# Test script to verify MCP server starts without errors

echo "Testing MCP Command Server startup..."

# Start the server in background and capture output
timeout 5s java -jar build/libs/mcp-server-command-0.5.0.jar --log-level DEBUG > test-output/server.log 2>&1 &
SERVER_PID=$!

# Give server time to start
sleep 2

# Check if server is still running
if ps -p $SERVER_PID > /dev/null; then
    echo "✓ Server started successfully"
    # Kill the server
    kill $SERVER_PID 2>/dev/null
else
    echo "✗ Server failed to start"
fi

# Check log for errors
if grep -q "Exception in thread" test-output/server.log; then
    echo "✗ Found exceptions in server log:"
    grep -A 5 "Exception in thread" test-output/server.log
    exit 1
else
    echo "✓ No exceptions found in server log"
fi

# Check for our specific deserialization error
if grep -q "Cannot construct instance" test-output/server.log; then
    echo "✗ Found deserialization error in server log"
    exit 1
else
    echo "✓ No deserialization errors found"
fi

echo ""
echo "Server log output:"
echo "=================="
cat test-output/server.log

exit 0
