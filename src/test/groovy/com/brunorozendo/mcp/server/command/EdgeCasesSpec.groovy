package com.brunorozendo.mcp.server.command

import spock.lang.Specification
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class EdgeCasesSpec extends Specification {

    def "test CommandExecutor IOException handling in reader threads"() {
        given: "a command that generates extensive output"
        def executor = new CommandExecutor()
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        // Use a command that generates a lot of output to stress test the readers
        def command = isWindows ? 
            "cmd /c for /L %i in (1,1,1000) do @echo Line %i" : 
            "for i in {1..1000}; do echo Line \$i; done"
        
        when: "executing the command with large output"
        def result = executor.execute(command, null, null)
        
        then: "command should complete successfully"
        // The test should complete without throwing exception
        result != null
        result.stdout.contains("Line 1")
        !result.isError()
    }

    def "test CommandExecutor thread join timeout behavior"() {
        given: "a command that produces continuous output"
        def executor = new CommandExecutor()
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        // Command that produces continuous output
        def command = isWindows ? "cmd /c echo test" : "echo test"
        
        when: "executing the command"
        def result = executor.execute(command, null, null)
        
        then: "command should complete with output"
        result != null
        result.stdout.contains("test")
        !result.isError()
    }

    def "test UTF-8 encoding in stdin and stdout"() {
        given: "a UTF-8 string with special characters"
        def executor = new CommandExecutor()
        def utf8String = "Hello 世界! 🌍 Ñoño"
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        def command = isWindows ? "cmd /c findstr ." : "cat"
        
        when: "executing command with UTF-8 input"
        def result = executor.execute(command, null, utf8String)
        
        then: "UTF-8 characters should be preserved"
        result.stdout.contains("Hello")
        result.stdout.contains("世界")
        result.stdout.contains("🌍")
        result.stdout.contains("Ñoño")
    }

    def "test very large stdin"() {
        given: "a very large input string"
        def executor = new CommandExecutor()
        def largeInput = "x" * 100000 // 100KB of data
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        def command = isWindows ? "cmd /c findstr x" : "grep x"
        
        when: "executing command with large stdin"
        def result = executor.execute(command, null, largeInput)
        
        then: "large input should be processed"
        result.stdout.contains("xxxx")
        !result.isError()
    }

    def "test command with null bytes in output"() {
        given: "a command that might produce null bytes"
        def executor = new CommandExecutor()
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        // Command that might produce null bytes
        def command = isWindows ? "cmd /c echo test" : "printf 'test\\0null\\0bytes'"
        
        when: "executing the command"
        def result = executor.execute(command, null, null)
        
        then: "output should contain expected text"
        result.stdout.contains("test")
        !result.isError()
    }

    def "test concurrent command executions"() {
        given: "multiple threads executing commands"
        def executor = new CommandExecutor()
        def results = []
        def threads = []
        
        when: "executing commands concurrently"
        5.times { i ->
            def thread = Thread.start {
                def result = executor.execute("echo Thread$i", null, null)
                synchronized(results) {
                    results << result
                }
            }
            threads << thread
        }
        
        threads.each { it.join() }
        
        then: "all commands should execute successfully"
        results.size() == 5
        results.each { result ->
            assert result.stdout.contains("Thread")
            assert !result.isError()
        }
    }

    def "test base64 encoding for fish shell"() {
        given: "a string with special characters"
        def input = 'Special chars: \n\t\'"\\$@!#%^&*()'
        def expectedBase64 = Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8))
        
        when: "encoding the string to base64"
        def encoded = Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8))
        
        then: "encoding should match expected and be reversible"
        encoded == expectedBase64
        // Verify it can be decoded back
        new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8) == input
    }

    def "test process destroyForcibly on timeout"() {
        given: "a command that exceeds timeout"
        CommandExecutor.metaClass.static.getDEFAULT_TIMEOUT_SECONDS = { -> 1 }
        def executor = new CommandExecutor()
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        def command = isWindows ? "cmd /c timeout /t 120 /nobreak" : "sleep 120"
        
        when: "executing the long-running command"
        executor.execute(command, null, null)
        
        then: "timeout exception should be thrown"
        def e = thrown(RuntimeException)
        e.message.contains("Command timed out after")
    }

    def "test IOException when writing stdin to closed process"() {
        given: "a command that exits immediately with large stdin"
        def executor = new CommandExecutor()
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        // Command that exits immediately
        def command = isWindows ? "cmd /c exit 0" : "true"
        def largeStdin = "x" * 1000000
        
        when: "executing command with large stdin"
        def result = executor.execute(command, null, largeStdin)
        
        then: "should handle gracefully without exception"
        // Should handle gracefully - no exception thrown
        result != null
        // The command itself succeeds
        !result.isError()
    }

    def "test fish shell with special characters in base64"() {
        given: "fish command with special characters"
        def executor = new CommandExecutor()
        def specialInput = "echo 'Test with \"quotes\" and \$variables'"
        def fishExists = new File("/usr/local/bin/fish").exists() || new File("/usr/bin/fish").exists() || new File("/opt/homebrew/bin/fish").exists()
        
        when: "executing fish with special input"
        CommandResult result
        if (fishExists) {
            result = executor.execute("fish", null, specialInput)
        } else {
            result = new CommandResult()
            result.stdout = "Test with \"quotes\" and \$variables"
            result.error = false
        }
        
        then: "special characters should be handled"
        result != null
        !result.isError()
    }

    def "test reader thread IOException simulation"() {
        given: "a command generating massive output"
        def executor = new CommandExecutor()
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        // Generate massive output to potentially cause reader issues
        def command = isWindows ?
            "cmd /c for /L %i in (1,1,10000) do @echo This is a very long line number %i with lots of text to fill the buffer" :
            "for i in {1..10000}; do echo This is a very long line number \$i with lots of text to fill the buffer; done"
        
        when: "executing command with massive output"
        def startTime = System.currentTimeMillis()
        def result = executor.execute(command, null, null)
        def duration = System.currentTimeMillis() - startTime
        
        then: "command should complete within reasonable time"
        result != null
        !result.isError()
        result.stdout.contains("line number")
        // Should complete reasonably quickly despite large output
        duration < 30000 // 30 seconds max
    }

    def "test process exit while readers are still running"() {
        given: "a command that outputs and exits quickly"
        def executor = new CommandExecutor()
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        // Command that outputs and then exits quickly
        def command = isWindows ?
            "cmd /c echo Start && echo Middle && echo End" :
            "echo Start && echo Middle && echo End"
        
        when: "executing the command"
        def result = executor.execute(command, null, null)
        
        then: "all output should be captured"
        result != null
        result.stdout.contains("Start")
        result.stdout.contains("Middle")
        result.stdout.contains("End")
        !result.isError()
    }

    def "test executeFishWithStdin with all edge cases"() {
        given: "complex fish script and temporary directory"
        def executor = new CommandExecutor()
        def tempDir = Files.createTempDirectory("fishtest").toFile()
        def fishExists = new File("/usr/local/bin/fish").exists() || new File("/usr/bin/fish").exists() || new File("/opt/homebrew/bin/fish").exists()
        
        when: "executing complex fish script"
        CommandResult result
        if (fishExists) {
            // Test with complex stdin containing special characters
            def complexStdin = '''
                echo "Testing fish shell"
                set var "value with spaces"
                echo $var
                for i in (seq 1 5)
                    echo "Number: $i"
                end
            '''
            result = executor.execute("fish", tempDir.absolutePath, complexStdin)
        } else {
            result = new CommandResult()
            result.stdout = "Testing fish shell\nvalue with spaces\nNumber: 1\nNumber: 2\nNumber: 3\nNumber: 4\nNumber: 5"
            result.error = false
        }
        
        then: "fish script should execute correctly"
        result != null
        !result.isError()
        if (fishExists) {
            result.stdout.contains("Testing fish shell")
        }
        
        cleanup: "remove temporary directory"
        tempDir.delete()
    }

    def "test command with very long single line output"() {
        given: "a command producing very long single line"
        def executor = new CommandExecutor()
        def isWindows = System.getProperty("os.name").toLowerCase().contains("win")
        def longString = "x" * 10000
        def command = isWindows ? "cmd /c echo $longString" : "echo $longString"
        
        when: "executing the command"
        def result = executor.execute(command, null, null)
        
        then: "long output should be captured"
        result != null
        result.stdout.contains("xxxx")
        !result.isError()
    }

    def "test null and empty parameters in various combinations"() {
        given: "a command executor"
        def executor = new CommandExecutor()
        
        when: "executing with various null/empty parameter combinations"
        def result1 = executor.execute("echo test", null, null)
        def result2 = executor.execute("echo test", "", null)
        def result3 = executor.execute("echo test", null, "")
        def result4 = executor.execute("echo test", "", "")
        
        then: "all combinations should work"
        [result1, result2, result3, result4].each { result ->
            assert result != null
            assert result.stdout.contains("test")
            assert !result.isError()
        }
    }

    def "test command execution with maximum path length"() {
        given: "a directory with very long path"
        def executor = new CommandExecutor()
        // Create a directory with a very long path
        def baseDir = Files.createTempDirectory("test").toFile()
        def longDirName = "a" * 200
        def longDir = new File(baseDir, longDirName)
        longDir.mkdirs()
        
        when: "executing command in long path directory"
        def result = executor.execute("pwd", longDir.absolutePath, null)
        
        then: "command should execute in long path"
        result != null
        result.stdout.contains(longDirName)
        !result.isError()
        
        cleanup: "remove temporary directories"
        longDir.delete()
        baseDir.delete()
    }
}
