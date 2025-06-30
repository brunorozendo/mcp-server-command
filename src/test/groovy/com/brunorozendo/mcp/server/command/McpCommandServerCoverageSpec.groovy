package com.brunorozendo.mcp.server.command

import spock.lang.Specification
import io.modelcontextprotocol.server.McpSyncServerExchange
import io.modelcontextprotocol.spec.McpSchema

class McpCommandServerCoverageSpec extends Specification {
    
    def "test McpCommandServer constructor"() {
        when: "creating a new McpCommandServer"
        def server = new McpCommandServer()
        
        then: "server should be created with initialized executor"
        server != null
        // Verify command executor is initialized
        def executorField = McpCommandServer.class.getDeclaredField("commandExecutor")
        executorField.setAccessible(true)
        executorField.get(server) != null
    }
    
    def "test start method coverage"() {
        given: "a new server instance"
        def server = new McpCommandServer()
        def started = false
        
        when: "starting the server in a thread"
        def thread = Thread.start {
            try {
                // Call start method
                def startMethod = McpCommandServer.class.getDeclaredMethod("start")
                startMethod.setAccessible(true)
                startMethod.invoke(server)
            } catch (Exception e) {
                // Expected when interrupted
                started = true
            }
        }
        
        Thread.sleep(500)
        thread.interrupt()
        thread.join(2000)
        
        then: "thread should terminate after interruption"
        !thread.isAlive()
    }
    
    def "test createRunCommandTool method"() {
        given: "a server instance"
        def server = new McpCommandServer()
        
        when: "creating the run command tool"
        def tool = server.createRunCommandTool()
        
        then: "tool should be properly configured"
        tool != null
        tool.name == "run_command"
        tool.description.contains(System.getProperty("os.name"))
        tool.inputSchema != null
        tool.inputSchema.type == "object"
        tool.inputSchema.properties.size() == 3
        tool.inputSchema.required == ["command"]
        tool.inputSchema.additionalProperties == false
    }
    
    def "test handleRunCommand with all output combinations"() {
        given: "a server instance and mock exchange"
        def server = new McpCommandServer()
        def mockExchange = Mock(McpSyncServerExchange)
        
        when: "handling command with message only (error case)"
        def args1 = [command: "exit 1"]
        def result1 = server.handleRunCommand(mockExchange, args1)
        
        then: "error result should contain failure message"
        result1.isError
        result1.content.size() >= 1
        result1.content.any { it.text.contains("Command failed with exit code: 1") }
        
        when: "handling command with stdout and stderr"
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        def args2 = [command: isWindows ? "cmd /c echo stdout & echo stderr 1>&2" : "echo stdout && echo stderr >&2"]
        def result2 = server.handleRunCommand(mockExchange, args2)
        
        then: "output should be captured"
        !result2.isError
        result2.content.size() >= 1
        result2.content.any { it.text.contains("stdout") }
        
        when: "handling command with empty output"
        def args3 = [command: isWindows ? "cmd /c rem" : "true"]
        def result3 = server.handleRunCommand(mockExchange, args3)
        
        then: "result should be successful with no content"
        !result3.isError
        result3.content.isEmpty()
    }
    
    def "test handleRunCommand exception handling"() {
        given: "server instance and invalid directory"
        def server = new McpCommandServer()
        def mockExchange = Mock(McpSyncServerExchange)
        
        when: "handling command with invalid working directory"
        def args = [command: "echo test", workdir: "/invalid/path"]
        def result = server.handleRunCommand(mockExchange, args)
        
        then: "error should be properly reported"
        result.isError
        result.content.size() == 1
        result.content[0].text.contains("Cannot run program")
    }
    
    def "test static main method"() {
        given: "a thread to run main method"
        def thread = null
        
        when: "running main method and interrupting"
        thread = Thread.start {
            try {
                McpCommandServer.main([] as String[])
            } catch (Exception e) {
                // Expected
            }
        }
        
        Thread.sleep(500)
        thread.interrupt()
        thread.join(2000)
        
        then: "thread should terminate"
        !thread.isAlive()
    }
    
    def "test static constants"() {
        expect: "constants should have correct values"
        McpCommandServer.PACKAGE_NAME == "mcp-server-command"
        McpCommandServer.PACKAGE_VERSION == "0.5.0"
    }
    
    def "test logger initialization"() {
        expect: "logger should be initialized"
        McpCommandServer.logger != null
    }
}
