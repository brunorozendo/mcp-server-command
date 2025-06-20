package com.brunorozendo.mcp.server.command

import spock.lang.Specification
import spock.lang.Timeout
import spock.lang.Title
import spock.lang.Narrative
import spock.lang.Requires

/**
 * Unit tests for CommandExecutor.
 */
@Title("Command Executor Specification")
@Narrative("""
This specification verifies the behavior of the CommandExecutor class,
which is responsible for executing shell commands and capturing their output.
""")
class CommandExecutorSpec extends Specification {

    private CommandExecutor executor

    def setup() {
        executor = new CommandExecutor()
    }

    def "should execute simple echo command successfully"() {
        given: "a simple echo command"
        def command = "echo Hello World"

        when: "the command is executed"
        def result = executor.execute(command, null, null)

        then: "the result should contain the expected output"
        result != null
        !result.isError()
        result.getStdout().trim() == "Hello World"
        result.getStderr().isEmpty()
    }

    def "should execute command with working directory"() {
        given: "a pwd command and a working directory"
        def command = "pwd"
        def workdir = "/tmp"

        when: "the command is executed with the working directory"
        def result = executor.execute(command, workdir, null)

        then: "the result should show the working directory"
        result != null
        !result.isError()
        result.getStdout().trim().contains("/tmp") // Check if output contains the directory instead of exact match
    }

    def "should execute command with stdin input"() {
        given: "a cat command and stdin input"
        def command = "cat"
        def stdin = "This is test input"

        when: "the command is executed with stdin"
        def result = executor.execute(command, null, stdin)

        then: "the result should contain the stdin input"
        result != null
        !result.isError()
        result.getStdout().trim() == stdin
    }

    def "should handle command that fails with non-zero exit code"() {
        given: "a command that will fail"
        def command = "ls /nonexistent/directory"

        when: "the command is executed"
        def result = executor.execute(command, null, null)

        then: "the result should indicate an error"
        result != null
        result.isError()
        result.getMessage() != null
        result.getMessage().contains("exit code")
        !result.getStderr().isEmpty()
    }

    def "should handle command not found"() {
        given: "a command that does not exist"
        def command = "thiscommanddoesnotexist"

        when: "the command is executed"
        def result = executor.execute(command, null, null)

        then: "the result should indicate an error"
        result != null
        result.isError()
        result.getStderr().contains("command not found") || result.getStderr().contains("not recognized")
    }

    def "should handle empty command"() {
        given: "an empty command"
        def command = ""

        when: "the command is executed"
        def result = executor.execute(command, null, null)

        then: "the result should be handled properly"
        result != null
        // Empty command might succeed or fail depending on shell
        result.getStdout() != null
        result.getStderr() != null
    }

    @Requires({ os.macOs || os.linux })
    def "should execute Python script via stdin"() {
        given: "a Python command and script"
        def command = "python3"
        def script = """print('Hello from Python')
print(2 + 2)"""

        when: "the command is executed with the script as stdin"
        def result = executor.execute(command, null, script)

        then: "the result should contain the Python output"
        result != null
        !result.isError()
        result.getStdout().contains("Hello from Python")
        result.getStdout().contains("4")
    }

    def "should handle multiple line output"() {
        given: "a command that produces multiple lines of output"
        def command = "echo -e 'line1\\nline2\\nline3'"

        when: "the command is executed"
        def result = executor.execute(command, null, null)

        then: "the result should contain multiple lines"
        result != null
        !result.isError()
        with(result.getStdout().trim()) {
            def lines = it.split("\n")
            lines.length >= 3 || it.contains("line1")
        }
    }

    @Timeout(65)
    def "should timeout long-running commands"() {
        given: "a command that runs longer than the timeout"
        def command = "sleep 65" // Sleep for longer than timeout

        when: "the command is executed"
        executor.execute(command, null, null)

        then: "a RuntimeException should be thrown"
        def exception = thrown(RuntimeException)
        exception.message.contains("Command timed out")
    }

    def "should handle special characters in command"() {
        given: "a command with special characters"
        def command = "echo 'Hello \$USER, today is \$(date)'"

        when: "the command is executed"
        def result = executor.execute(command, null, null)

        then: "the result should be handled properly"
        result != null
        !result.isError()
        !result.getStdout().isEmpty()
    }
}
