package com.brunorozendo.mcp.server.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CommandExecutor {
    public static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);
    public static final int DEFAULT_TIMEOUT_SECONDS = 60;

    public CommandResult execute(String command, String workdir, String stdin) throws IOException, InterruptedException {
        logger.debug("Executing command: {}, workdir: {}, stdin: {}", command, workdir, stdin != null ? "provided" : "none");

        // Special handling for fish shell with stdin
        if (stdin != null && command.split(" ")[0].equals("fish")) {
            return executeFishWithStdin(command, workdir, stdin);
        }

        ProcessBuilder processBuilder = new ProcessBuilder();

        // Parse command into executable and arguments
        List<String> commandList = parseCommand(command);
        processBuilder.command(commandList);

        // Set working directory if provided
        if (workdir != null && !workdir.isEmpty()) {
            processBuilder.directory(new File(workdir));
        }

        // Start the process
        Process process = processBuilder.start();

        // Write stdin if provided
        if (stdin != null) {
            try (Writer writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(stdin);
                writer.flush();
            } catch (IOException e) {
                // Handle the case where the process has already exited and closed its input stream
                logger.debug("Failed to write stdin to process (process may have already exited): {}", e.getMessage());
                // Continue execution - this is not a fatal error
            }
        } else {
            // Close stdin to prevent commands like 'cat' from hanging
            try {
                process.getOutputStream().close();
            } catch (IOException e) {
                // Process may have already closed - not a fatal error
                logger.debug("Failed to close process output stream: {}", e.getMessage());
            }
        }

        // Read output and error streams
        return getCommandResult(process);
    }

    private static CommandResult getCommandResult(Process process) throws IOException, InterruptedException {
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();

        try (BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
             BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {

            // Read stdout in a separate thread to avoid blocking
            Thread stdoutThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = stdoutReader.readLine()) != null) {
                        stdout.append(line).append("\n");
                    }
                } catch (IOException e) {
                    logger.error("Error reading stdout", e);
                }
            });

            // Read stderr in a separate thread to avoid blocking
            Thread stderrThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = stderrReader.readLine()) != null) {
                        stderr.append(line).append("\n");
                    }
                } catch (IOException e) {
                    logger.error("Error reading stderr", e);
                }
            });

            stdoutThread.start();
            stderrThread.start();

            // Wait for process to complete
            boolean completed = process.waitFor(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                throw new RuntimeException("Command timed out after " + DEFAULT_TIMEOUT_SECONDS + " seconds");
            }

            // Wait for readers to finish
            stdoutThread.join(1000);
            stderrThread.join(1000);
        }

        int exitCode = process.exitValue();
        CommandResult result = new CommandResult();
        result.setStdout(stdout.toString().trim());
        result.setStderr(stderr.toString().trim());
        result.setError(exitCode != 0);

        if (exitCode != 0) {
            result.setMessage("Command failed with exit code: " + exitCode);
        }

        return result;
    }

    private CommandResult executeFishWithStdin(String command, String workdir, String stdin) throws IOException, InterruptedException {
        // Fish shell has issues with piped input, so we use a workaround
        String base64Stdin = Base64.getEncoder().encodeToString(stdin.getBytes(StandardCharsets.UTF_8));
        String wrappedCommand = command + " -c \"echo " + base64Stdin + " | base64 -d | fish\"";

        // Use sh to execute the wrapped command
        List<String> commandList = List.of("/bin/sh", "-c", wrappedCommand);

        ProcessBuilder processBuilder = new ProcessBuilder(commandList);

        if (workdir != null && !workdir.isEmpty()) {
            processBuilder.directory(new File(workdir));
        }

        Process process = processBuilder.start();

        // Read output and error streams
        return getCommandResult(process);
    }

    private List<String> parseCommand(String command) {
        // Use the system shell to handle complex command parsing
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String shell = isWindows ? "cmd.exe" : "/bin/sh";
        String shellFlag = isWindows ? "/c" : "-c";

        return List.of(shell, shellFlag, command);
    }
}