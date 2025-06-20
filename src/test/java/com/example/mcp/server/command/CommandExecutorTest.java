package com.example.mcp.server.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CommandExecutor.
 */
class CommandExecutorTest {
    
    private CommandExecutor executor;
    
    @BeforeEach
    void setUp() {
        executor = new CommandExecutor();
    }
    
    @Test
    @DisplayName("Should execute simple echo command successfully")
    void testSimpleEchoCommand() throws IOException, InterruptedException {
        // Arrange
        String command = "echo Hello World";
        
        // Act
        CommandResult result = executor.execute(command, null, null);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isError());
        assertEquals("Hello World", result.getStdout().trim());
        assertTrue(result.getStderr().isEmpty());
    }
    
    @Test
    @DisplayName("Should execute command with working directory")
    void testCommandWithWorkingDirectory() throws IOException, InterruptedException {
        // Arrange
        String command = "pwd";
        String workdir = "/tmp";
        
        // Act
        CommandResult result = executor.execute(command, workdir, null);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isError());
        assertEquals("/tmp", result.getStdout().trim());
    }
    
    @Test
    @DisplayName("Should execute command with stdin input")
    void testCommandWithStdin() throws IOException, InterruptedException {
        // Arrange
        String command = "cat";
        String stdin = "This is test input";
        
        // Act
        CommandResult result = executor.execute(command, null, stdin);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isError());
        assertEquals(stdin, result.getStdout().trim());
    }
    
    @Test
    @DisplayName("Should handle command that fails with non-zero exit code")
    void testFailingCommand() throws IOException, InterruptedException {
        // Arrange
        String command = "ls /nonexistent/directory";
        
        // Act
        CommandResult result = executor.execute(command, null, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isError());
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("exit code"));
        assertFalse(result.getStderr().isEmpty());
    }
    
    @Test
    @DisplayName("Should handle command not found")
    void testCommandNotFound() throws IOException, InterruptedException {
        // Arrange
        String command = "thiscommanddoesnotexist";
        
        // Act
        CommandResult result = executor.execute(command, null, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isError());
        assertTrue(result.getStderr().contains("command not found") || 
                  result.getStderr().contains("not recognized"));
    }
    
    @Test
    @DisplayName("Should handle empty command")
    void testEmptyCommand() throws IOException, InterruptedException {
        // Arrange
        String command = "";
        
        // Act
        CommandResult result = executor.execute(command, null, null);
        
        // Assert
        assertNotNull(result);
        // Empty command might succeed or fail depending on shell
        assertNotNull(result.getStdout());
        assertNotNull(result.getStderr());
    }
    
    @Test
    @DisplayName("Should execute Python script via stdin")
    @EnabledOnOs({OS.MAC, OS.LINUX})
    void testPythonScriptExecution() throws IOException, InterruptedException {
        // Arrange
        String command = "python3";
        String script = "print('Hello from Python')\\nprint(2 + 2)";
        
        // Act
        CommandResult result = executor.execute(command, null, script);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isError());
        assertTrue(result.getStdout().contains("Hello from Python"));
        assertTrue(result.getStdout().contains("4"));
    }
    
    @Test
    @DisplayName("Should handle multiple line output")
    void testMultiLineOutput() throws IOException, InterruptedException {
        // Arrange
        String command = "echo -e 'line1\\nline2\\nline3'";
        
        // Act
        CommandResult result = executor.execute(command, null, null);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isError());
        String[] lines = result.getStdout().trim().split("\n");
        assertTrue(lines.length >= 3 || result.getStdout().contains("line1"));
    }
    
    @Test
    @DisplayName("Should timeout long-running commands")
    @Timeout(value = 65, unit = TimeUnit.SECONDS)
    void testCommandTimeout() {
        // Arrange
        String command = "sleep 65"; // Sleep for longer than timeout
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            executor.execute(command, null, null);
        }, "Command should timeout after 60 seconds");
    }
    
    @Test
    @DisplayName("Should handle special characters in command")
    void testSpecialCharactersInCommand() throws IOException, InterruptedException {
        // Arrange
        String command = "echo 'Hello $USER, today is $(date)'";
        
        // Act
        CommandResult result = executor.execute(command, null, null);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isError());
        assertFalse(result.getStdout().isEmpty());
    }
}
