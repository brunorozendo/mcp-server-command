#!/bin/bash

# Test if MCP server can initialize without deserialization errors

echo "Testing MCP Command Server initialization..."

# Create a test initialization message
cat > test-output/init.json << 'EOF'
{"jsonrpc":"2.0","method":"initialize","id":0,"params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test-client","version":"1.0.0"}}}
EOF

# Start server and send initialization
echo "Starting server and sending initialization request..."
(echo '{"jsonrpc":"2.0","method":"initialize","id":0,"params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test-client","version":"1.0.0"}}}'; sleep 2) | timeout 3s java -jar build/libs/mcp-server-command-0.5.0.jar --log-level INFO > test-output/init-test.log 2>&1

# Check results
if grep -q "Cannot construct instance" test-output/init-test.log; then
    echo "✗ FAILED: Deserialization error found!"
    echo "Error details:"
    grep -C 5 "Cannot construct instance" test-output/init-test.log
    exit 1
elif grep -q "Server started" test-output/init-test.log; then
    echo "✓ SUCCESS: Server initialized without deserialization errors!"
    echo ""
    echo "Server response:"
    grep -E '{"jsonrpc"|Server started' test-output/init-test.log
    exit 0
else
    echo "? UNKNOWN: Could not determine server status"
    echo "Log contents:"
    cat test-output/init-test.log
    exit 2
fi
