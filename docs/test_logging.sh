#!/bin/bash

# Test script for MCP Server Command logging

echo "Testing MCP Server Command logging..."

# Always rebuild the project to include the latest changes
echo "Building the project..."
./gradlew clean build

# Function to test logging with a specific directory
test_logging() {
    local dir_name=$1
    local system_property=$2

    echo "Testing with logs directory: $dir_name"

    # Create the directory
    rm -rf "$dir_name"
    mkdir -p "$dir_name"
    echo "Created directory: $dir_name"

    # Run the server with the specified configuration
    echo "Starting server..."
    if [ -z "$system_property" ]; then
        # Default logs directory
        java -Dfile.logging.enabled=true -jar build/libs/mcp-server-command-0.5.0.jar --log-level DEBUG > /dev/null 2>&1 &
    else
        # Custom logs directory
        java -Dfile.logging.enabled=true $system_property -jar build/libs/mcp-server-command-0.5.0.jar --log-level DEBUG > /dev/null 2>&1 &
    fi

    SERVER_PID=$!
    echo "Server started with PID: $SERVER_PID"

    # Give the server time to initialize
    sleep 10

    # Check if the server is still running
    if kill -0 $SERVER_PID 2>/dev/null; then
        echo "Server is running, stopping it..."
        kill $SERVER_PID
        sleep 2
    else
        echo "Server is not running"
    fi

    # Check if logs were created
    echo "Checking if logs were created in $dir_name..."
    if [ -f "$dir_name/application.log" ]; then
        echo "SUCCESS: Log file was created"
        echo "Log file content:"
        head -n 10 "$dir_name/application.log"
    else
        echo "ERROR: Log file was not created"
        echo "Directory contents:"
        ls -la "$dir_name"
    fi
}

# Test 1: Default logs directory
echo "Test 1: Using default logs directory"
test_logging "logs" ""

# Test 2: Custom logs directory
echo -e "\nTest 2: Using custom logs directory"
test_logging "custom_logs" "-Dlogs.dir=custom_logs"

echo -e "\nAll logging tests completed."
