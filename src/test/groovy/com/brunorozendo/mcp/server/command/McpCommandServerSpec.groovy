package com.brunorozendo.mcp.server.command

import io.modelcontextprotocol.server.McpSyncServerExchange
import io.modelcontextprotocol.spec.McpSchema
import spock.lang.Specification
import spock.lang.Unroll

class McpCommandServerSpec extends Specification {

    McpCommandServer server
    McpSyncServerExchange mockExchange

    def setup() {
        server = new McpCommandServer()
        mockExchange = Mock(McpSyncServerExchange)
    }

    def "test constructor initializes command executor"() {
        when: "creating a new McpCommandServer"
        def newServer = new McpCommandServer()

        then: "command executor should be initialized"
        // Access private field to verify initialization
        def executorField = McpCommandServer.class.getDeclaredField("commandExecutor")
        executorField.setAccessible(true)
        executorField.get(newServer) != null
        executorField.get(newServer) instanceof CommandExecutor
    }

    def "test createRunCommandTool creates tool with proper schema"() {
        when: "creating the run command tool"
        def tool = server.createRunCommandTool()

        then: "tool should have correct name and description"
        tool.name == "run_command"
        tool.description == "Run a command on this " + System.getProperty("os.name") + " machine"
        
        and: "tool schema should be properly configured"
        // Verify schema
        tool.inputSchema != null
        tool.inputSchema.type == "object"
        tool.inputSchema.properties != null
        tool.inputSchema.properties.size() == 3
        
        and: "command property should be defined"
        // Verify command property
        def commandProp = tool.inputSchema.properties["command"]
        commandProp["type"] == "string"
        commandProp["description"] == "Command with args"
        
        and: "workdir property should be defined"
        // Verify workdir property
        def workdirProp = tool.inputSchema.properties["workdir"]
        workdirProp["type"] == "string"
        workdirProp["description"] == "Optional, current working directory"
        
        and: "stdin property should be defined"
        // Verify stdin property
        def stdinProp = tool.inputSchema.properties["stdin"]
        stdinProp["type"] == "string"
        stdinProp["description"].contains("Optional, text to pipe into the command's STDIN")
        
        and: "required fields should be set"
        // Verify required fields
        tool.inputSchema.required == ["command"]
        tool.inputSchema.additionalProperties == false
    }

    def "test handleRunCommand with valid command"() {
        given: "a valid command argument"
        def args = [command: "echo test", workdir: null, stdin: null]
        
        // Create a real CommandExecutor to avoid mocking issues
        def executorField = McpCommandServer.class.getDeclaredField("commandExecutor")
        executorField.setAccessible(true)
        def realExecutor = executorField.get(server)

        when: "handling the run command"
        def result = server.handleRunCommand(mockExchange, args)

        then: "command should execute successfully"
        !result.isError
        result.content.size() >= 1
        result.content[0] instanceof McpSchema.TextContent
        (result.content[0] as McpSchema.TextContent).text.contains("test")
    }

    def "test handleRunCommand with error result"() {
        given: "a command that will fail"
        def args = [command: "exit 1", workdir: null, stdin: null]

        when: "handling the failing command"
        def result = server.handleRunCommand(mockExchange, args)

        then: "error should be properly reported"
        result.isError
        result.content.size() >= 1
        result.content.any { it.text.contains("Command failed with exit code: 1") }
    }

    def "test handleRunCommand with all parameters"() {
        given: "a temporary directory with test file"
        def tempDir = File.createTempDir()
        def testFile = new File(tempDir, "test.txt")
        testFile.text = "file content"
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        def command = isWindows ? "cmd /c type test.txt" : "cat test.txt"
        def args = [command: command, workdir: tempDir.absolutePath, stdin: null]

        when: "handling command with working directory"
        def result = server.handleRunCommand(mockExchange, args)

        then: "file should be read from working directory"
        !result.isError
        result.content.size() >= 1
        result.content[0].text.contains("file content")

        cleanup: "remove temporary files"
        testFile.delete()
        tempDir.delete()
    }

    def "test handleRunCommand with stdout and stderr"() {
        given: "a command that outputs to both stdout and stderr"
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        def command = isWindows ?
            "cmd /c echo stdout message & echo stderr message 1>&2" :
            "echo 'stdout message' && echo 'stderr message' >&2"
        def args = [command: command]

        when: "handling the command"
        def result = server.handleRunCommand(mockExchange, args)

        then: "stdout should be captured"
        !result.isError
        result.content.size() >= 1
        result.content.any { it.text.contains("stdout message") }
    }

    def "test handleRunCommand with stdin"() {
        given: "a command that reads from stdin"
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        def command = isWindows ? "cmd /c findstr ." : "cat"
        def args = [command: command, stdin: "Hello from stdin"]

        when: "handling command with stdin"
        def result = server.handleRunCommand(mockExchange, args)

        then: "stdin content should be processed"
        !result.isError
        result.content.size() >= 1
        result.content[0].text.contains("Hello from stdin")
    }

    @Unroll
    def "test handleRunCommand with invalid command: #description"() {
        given: "invalid command arguments"
        def args = argsMap

        when: "handling invalid command"
        def result = server.handleRunCommand(mockExchange, args)

        then: "appropriate error should be returned"
        result.isError
        result.content.size() == 1
        result.content[0].text == expectedMessage

        where:
        description          | argsMap                      | expectedMessage
        "null command"       | [command: null]              | "Command is required, current value: null"
        "empty command"      | [command: ""]                | "Command is required, current value: "
        "missing command"    | [:]                          | "Command is required, current value: null"
    }

    def "test handleRunCommand with IOException"() {
        given: "a command with invalid working directory"
        // Create an invalid working directory to trigger IOException
        def args = [command: "echo test", workdir: "/this/directory/does/not/exist"]

        when: "handling command with invalid directory"
        def result = server.handleRunCommand(mockExchange, args)

        then: "IOException should be properly handled"
        result.isError
        result.content.size() == 1
        result.content[0].text.contains("Cannot run program")
    }

    def "test handleRunCommand with empty stdout and stderr"() {
        given: "a command that produces no output"
        // Use a command that produces no output
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        def command = isWindows ? "cmd /c rem" : "true"
        def args = [command: command]

        when: "handling command with no output"
        def result = server.handleRunCommand(mockExchange, args)

        then: "result should be successful with no content"
        !result.isError
        result.content.isEmpty()
    }

    def "test main method"() {
        given: "stored system properties and simulated stdio"
        // Store original system properties
        def originalStdout = System.out
        def originalStderr = System.err
        def originalStdin = System.in
        
        // Create pipes to simulate stdio transport
        def stdoutPipe = new PipedOutputStream()
        def stderrPipe = new PipedOutputStream()
        def stdinPipe = new PipedInputStream()
        
        // Redirect stdio
        System.setOut(new PrintStream(stdoutPipe))
        System.setErr(new PrintStream(stderrPipe))
        System.setIn(stdinPipe)
        
        // Start server in a separate thread
        def serverThread = Thread.start {
            try {
                McpCommandServer.main([] as String[])
            } catch (InterruptedException e) {
                // Expected when we interrupt
            }
        }
        
        // Give server time to start
        Thread.sleep(100)

        when: "interrupting the server thread"
        // Interrupt the server thread
        serverThread.interrupt()
        serverThread.join(1000)

        then: "server should stop gracefully"
        // Server should have started and then been interrupted
        !serverThread.isAlive()

        cleanup: "restore original stdio"
        // Restore original stdio
        System.setOut(originalStdout)
        System.setErr(originalStderr)  
        System.setIn(originalStdin)
        stdoutPipe.close()
        stderrPipe.close()
        stdinPipe.close()
    }

    def "test start method creates server and handles interruption"() {
        given: "a server instance"
        def server = new McpCommandServer()
        def serverThread = null
        
        when: "starting the server and interrupting it"
        // Start server in a thread
        serverThread = Thread.start {
            try {
                server.start()
            } catch (Exception e) {
                // Expected
            }
        }
        
        // Give it time to start
        Thread.sleep(100)
        
        // Interrupt it
        serverThread.interrupt()
        serverThread.join(1000)

        then: "server thread should terminate"
        !serverThread.isAlive()
    }

    def "test package constants"() {
        expect: "package constants should have correct values"
        McpCommandServer.PACKAGE_NAME == "mcp-server-command"
        McpCommandServer.PACKAGE_VERSION == "0.5.0"
    }
}
