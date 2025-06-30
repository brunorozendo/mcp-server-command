package com.brunorozendo.mcp.server.command;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Implementation;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class McpCommandServer {
    private static final Logger logger = LoggerFactory.getLogger(McpCommandServer.class);

    public static final String PACKAGE_NAME = "mcp-server-command";
    public static final String PACKAGE_VERSION = "0.5.0";

    private final CommandExecutor commandExecutor;


    public McpCommandServer() {
        this.commandExecutor = new CommandExecutor();
    }


    public void start() {
        // Create the transport provider
        StdioServerTransportProvider transportProvider = new StdioServerTransportProvider();

        // Create the server
        McpSyncServer server = McpServer.sync(transportProvider)
            .serverInfo(new Implementation(PACKAGE_NAME, PACKAGE_VERSION))
            .instructions("Run commands on this " + System.getProperty("os.name") + " machine")
            .tool(createRunCommandTool(), this::handleRunCommand)
            .build();

        logger.info("MCP Command Server started");

        // Keep the server running
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            logger.error("Server interrupted", e);
            server.close();
        }
    }

    public Tool createRunCommandTool() {
        // Create properties map for the schema
        Map<String, Object> properties = new HashMap<>();
        
        // Define command property
        Map<String, Object> commandProp = new HashMap<>();
        commandProp.put("type", "string");
        commandProp.put("description", "Command with args");
        properties.put("command", commandProp);
        
        // Define workdir property
        Map<String, Object> workdirProp = new HashMap<>();
        workdirProp.put("type", "string");
        workdirProp.put("description", "Optional, current working directory");
        properties.put("workdir", workdirProp);
        
        // Define stdin property
        Map<String, Object> stdinProp = new HashMap<>();
        stdinProp.put("type", "string");
        stdinProp.put("description", "Optional, text to pipe into the command's STDIN. For example, pass a python script to python3. Or, pass text for a new file to the cat command to create it!");
        properties.put("stdin", stdinProp);
        
        // Create required array
        List<String> required = List.of("command");
        
        // Create the JsonSchema object
        JsonSchema schema = new JsonSchema(
            "object",           // type
            properties,         // properties
            required,          // required
            false,             // additionalProperties
            null,              // $defs
            null               // definitions
        );

        // Create the tool with the schema
        return new Tool("run_command", 
                       "Run a command on this " + System.getProperty("os.name") + " machine",
                       schema);
    }


    public CallToolResult handleRunCommand(McpSyncServerExchange exchange, Map<String, Object> args) {
        logger.debug("ToolRequest: {}", args);

        String command = (String) args.get("command");
        if (command == null || command.isEmpty()) {
            String message = "Command is required, current value: " + command;
            return new CallToolResult(message, true);
        }

        String workdir = (String) args.get("workdir");
        String stdin = (String) args.get("stdin");

        try {
            CommandResult result = commandExecutor.execute(command, workdir, stdin);
            List<Content> content = new ArrayList<>();

            if (result.getMessage() != null) {
                content.add(new TextContent(result.getMessage()));
            }
            if (!result.getStdout().isEmpty()) {
                content.add(new TextContent(result.getStdout()));
            }
            if (!result.getStderr().isEmpty()) {
                content.add(new TextContent(result.getStderr()));
            }

            return CallToolResult.builder()
                .content(content)
                .isError(result.isError())
                .build();

        } catch (Exception e) {
            logger.warn("run_command failed", e);
            return new CallToolResult(e.getMessage(), true);
        }
    }


    public static void main(String[] args) {
        McpCommandServer server = new McpCommandServer();
        server.start();
    }
}
