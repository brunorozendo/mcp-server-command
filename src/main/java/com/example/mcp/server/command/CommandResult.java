package com.example.mcp.server.command;

public class CommandResult {
    private String stdout = "";
    private String stderr = "";
    private String message;
    private boolean isError = false;
    
    public String getStdout() {
        return stdout;
    }
    
    public void setStdout(String stdout) {
        this.stdout = stdout != null ? stdout : "";
    }
    
    public String getStderr() {
        return stderr;
    }
    
    public void setStderr(String stderr) {
        this.stderr = stderr != null ? stderr : "";
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isError() {
        return isError;
    }
    
    public void setError(boolean error) {
        isError = error;
    }
    
    @Override
    public String toString() {
        return "CommandResult{" +
               "stdout='" + stdout + '\'' +
               ", stderr='" + stderr + '\'' +
               ", message='" + message + '\'' +
               ", isError=" + isError +
               '}';
    }
}
