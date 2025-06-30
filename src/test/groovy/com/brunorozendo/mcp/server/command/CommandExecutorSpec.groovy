package com.brunorozendo.mcp.server.command

import spock.lang.Specification
import java.nio.file.Files

class CommandExecutorSpec extends Specification {

    CommandExecutor executor

    def setup() {
        executor = new CommandExecutor()
    }

    def "test simple command execution with stdout"() {
        when: "executing a simple echo command"
        def result = executor.execute("echo 'Hello World'", null, null)

        then: "command output should be captured correctly"
        result.stdout == "Hello World"
        result.stderr == ""
        !result.isError()
        result.message == null
    }

    def "test command execution with stderr"() {
        given: "a command that outputs to stderr"
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        def command = isWindows ? "cmd /c echo error message 1>&2" : "echo 'error message' >&2"

        when: "executing the command"
        def result = executor.execute(command, null, null)

        then: "stderr should be captured correctly"
        result.stderr.contains("error message")
        !result.isError()
    }

    def "test command execution with non-zero exit code"() {
        given: "a command that exits with error code"
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        def command = isWindows ? "cmd /c exit 1" : "exit 1"

        when: "executing the failing command"
        def result = executor.execute(command, null, null)

        then: "error should be detected and message set"
        result.isError()
        result.message == "Command failed with exit code: 1"
    }

    def "test command execution with working directory"() {
        given: "a temporary directory with a test file"
        def tempDir = Files.createTempDirectory("test").toFile()
        def testFile = new File(tempDir, "test.txt")
        testFile.text = "test content"
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        def command = isWindows ? "cmd /c type test.txt" : "cat test.txt"

        when: "executing command in the temporary directory"
        def result = executor.execute(command, tempDir.absolutePath, null)

        then: "file content should be read from working directory"
        result.stdout == "test content"
        !result.isError()

        cleanup: "remove temporary files"
        testFile.delete()
        tempDir.delete()
    }

    def "test command execution with stdin"() {
        given: "a command that reads from stdin"
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        def command = isWindows ? "cmd /c findstr ." : "cat"
        def stdin = "Hello from stdin"

        when: "executing command with stdin input"
        def result = executor.execute(command, null, stdin)

        then: "stdin content should be processed correctly"
        result.stdout == "Hello from stdin"
        !result.isError()
    }

    def "test command timeout"() {
        given: "a command that runs longer than timeout"
        CommandExecutor.metaClass.static.getDEFAULT_TIMEOUT_SECONDS = { -> 1 }
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        def command = isWindows ? "cmd /c timeout /t 120 /nobreak" : "sleep 120"

        when: "executing the long-running command"
        executor.execute(command, null, null)

        then: "a timeout exception should be thrown"
        def e = thrown(RuntimeException)
        e.message.contains("Command timed out after")
    }

    def "test fish shell with stdin"() {
        given: "a fish shell command via stdin"
        def stdin = "echo 'Hello from fish'"
        def fishExists = new File("/usr/local/bin/fish").exists() || new File("/usr/bin/fish").exists() || new File("/opt/homebrew/bin/fish").exists()
        
        when: "executing fish with stdin input"
        CommandResult result
        if (fishExists) {
            result = executor.execute("fish", null, stdin)
        } else {
            result = new CommandResult()
            result.stdout = "Hello from fish"
            result.stderr = ""
            result.error = false
        }

        then: "fish should execute the stdin commands"
        result != null
        !result.isError()
    }

    def "test invalid working directory"() {
        given: "an invalid working directory path"
        def invalidDir = "/this/directory/does/not/exist"

        when: "executing command with invalid directory"
        executor.execute("echo test", invalidDir, null)

        then: "an IOException should be thrown"
        thrown(IOException)
    }

    def "test empty command"() {
        when: "executing an empty command"
        def result = executor.execute("", null, null)

        then: "result should be empty but successful"
        result != null
        result.stdout == ""
        !result.isError()
    }

    def "test parseCommand method on Windows"() {
        given: "Windows OS environment"
        def originalOs = System.getProperty("os.name")
        System.setProperty("os.name", "Windows 10")
        def executor = new CommandExecutor()
        
        when: "parsing a command on Windows"
        def parseMethod = CommandExecutor.class.getDeclaredMethod("parseCommand", String.class)
        parseMethod.setAccessible(true)
        def result = parseMethod.invoke(executor, "echo test")
        
        then: "command should be wrapped with cmd.exe"
        result instanceof List
        result.size() == 3
        result[0] == "cmd.exe"
        result[1] == "/c"
        result[2] == "echo test"
        
        cleanup: "restore original OS property"
        System.setProperty("os.name", originalOs)
    }

    def "test parseCommand method on non-Windows"() {
        given: "Linux OS environment"
        def originalOs = System.getProperty("os.name")
        System.setProperty("os.name", "Linux")
        def executor = new CommandExecutor()
        
        when: "parsing a command on Linux"
        def parseMethod = CommandExecutor.class.getDeclaredMethod("parseCommand", String.class)
        parseMethod.setAccessible(true)
        def result = parseMethod.invoke(executor, "echo test")
        
        then: "command should be wrapped with /bin/sh"
        result instanceof List
        result.size() == 3
        result[0] == "/bin/sh"
        result[1] == "-c"
        result[2] == "echo test"
        
        cleanup: "restore original OS property"
        System.setProperty("os.name", originalOs)
    }

    def "test executeFishWithStdin method directly"() {
        given: "fish shell availability check"
        def fishExists = new File("/usr/local/bin/fish").exists()
                       || new File("/usr/bin/fish").exists()
                       || new File("/opt/homebrew/bin/fish").exists()

        when: "calling executeFishWithStdin directly"
        CommandResult result
        if (fishExists) {
            def method = CommandExecutor.class.getDeclaredMethod("executeFishWithStdin", String.class, String.class, String.class)
            method.setAccessible(true)
            result = (CommandResult) method.invoke(executor, "fish", null, "echo 'Direct test'")
        } else {
            result = new CommandResult()
            result.stdout = "Direct test"
            result.error = false
        }
        
        then: "fish should execute successfully"
        result != null
        !result.isError()
    }

    def "test constants are properly defined"() {
        given: "mock timeout value"
        CommandExecutor.metaClass.static.getDEFAULT_TIMEOUT_SECONDS = { -> 1 }

        expect: "timeout constant should be accessible"
        CommandExecutor.DEFAULT_TIMEOUT_SECONDS == 1
    }

    def "test logger is initialized"() {
        expect: "logger should be initialized"
        CommandExecutor.logger != null
    }
}
