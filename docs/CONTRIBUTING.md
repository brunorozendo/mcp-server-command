# Contributing to MCP Command Server

Thank you for your interest in contributing to the MCP Command Server! This document provides guidelines and instructions for contributing to the project.

## Getting Started

### Prerequisites

- Java 21 or higher
- Gradle 8.x
- Git
- An IDE (IntelliJ IDEA recommended)

### Setting Up Development Environment

1. Fork and clone the repository:
```bash
git clone https://github.com/YOUR_USERNAME/mcp-server-command.git
cd mcp-server-command
```

2. Import the project into your IDE as a Gradle project

3. Build the project:
```bash
./gradlew build
```

4. Run tests:
```bash
./gradlew test
```

## Development Workflow

### 1. Create a Feature Branch

```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/your-bug-fix
```

### 2. Make Your Changes

Follow these coding standards:

#### Java Style Guide
- Use 4 spaces for indentation (no tabs)
- Maximum line length: 120 characters
- Follow standard Java naming conventions
- Add JavaDoc comments for public methods

#### Code Organization
```java
// Example class structure
package com.example.mcp.server.command;

import statements...

/**
 * Brief description of the class.
 */
public class ExampleClass {
    private static final Logger logger = LoggerFactory.getLogger(ExampleClass.class);
    
    // Constants
    private static final int CONSTANT_NAME = 42;
    
    // Instance variables
    private final String fieldName;
    
    // Constructor
    public ExampleClass(String fieldName) {
        this.fieldName = fieldName;
    }
    
    // Methods
    /**
     * Method description.
     * @param param Parameter description
     * @return Return value description
     */
    public String doSomething(String param) {
        // Implementation
    }
}
```

### 3. Write Tests

All new features must include tests:

```java
@Test
void testCommandExecution() {
    // Arrange
    CommandExecutor executor = new CommandExecutor();
    
    // Act
    CommandResult result = executor.execute("echo test", null, null);
    
    // Assert
    assertFalse(result.isError());
    assertEquals("test", result.getStdout());
}
```

### 4. Update Documentation

- Update README.md if adding new features
- Update API documentation for new tools
- Add inline code comments for complex logic

### 5. Commit Your Changes

Write clear, descriptive commit messages:

```bash
# Good
git commit -m "Add timeout configuration option for command execution"

# Bad
git commit -m "Fixed stuff"
```

Commit message format:
- First line: Brief summary (50 chars max)
- Blank line
- Detailed explanation if needed

### 6. Submit a Pull Request

1. Push your branch:
```bash
git push origin feature/your-feature-name
```

2. Create a pull request with:
   - Clear title and description
   - Link to related issues
   - List of changes made
   - Screenshots if applicable

## Types of Contributions

### Bug Fixes
1. Check existing issues first
2. Create a minimal reproduction case
3. Include fix with tests
4. Update documentation if needed

### New Features
1. Discuss in an issue first
2. Keep features focused and minimal
3. Add comprehensive tests
4. Update all relevant documentation

### Documentation
- Fix typos and grammar
- Improve examples
- Add clarifications
- Translate documentation

### Performance Improvements
- Include benchmarks
- Document the improvement
- Ensure no functionality is broken

## Testing Guidelines

### Unit Tests
- Test individual methods/classes
- Mock external dependencies
- Cover edge cases

### Integration Tests
- Test complete command execution
- Test error scenarios
- Test different platforms if possible

### Test Structure
```java
class CommandExecutorTest {
    
    @BeforeEach
    void setUp() {
        // Setup test environment
    }
    
    @Test
    void shouldExecuteSimpleCommand() {
        // Test implementation
    }
    
    @Test
    void shouldHandleCommandTimeout() {
        // Test implementation
    }
    
    @AfterEach
    void tearDown() {
        // Cleanup
    }
}
```

## Code Review Process

### What We Look For
- Code quality and readability
- Test coverage
- Documentation updates
- Performance impact
- Security considerations
- MCP protocol compliance

### Review Timeline
- Initial review within 3-5 days
- Address feedback promptly
- Re-review after changes

## Security Guidelines

### Reporting Security Issues
- Do NOT create public issues for security vulnerabilities
- Email security concerns to [security@example.com]
- Include detailed description and reproduction steps

### Security Considerations
When contributing, consider:
- Command injection risks
- Path traversal vulnerabilities
- Resource exhaustion
- Privilege escalation
- Information disclosure

## Adding New Tools

To add a new MCP tool:

1. Define the tool schema:
```java
private Tool createMyTool() {
    String schema = """
        {
            "type": "object",
            "properties": {
                "param1": {
                    "type": "string",
                    "description": "Parameter description"
                }
            },
            "required": ["param1"]
        }
        """;
    return new Tool("my_tool", "Tool description", schema);
}
```

2. Implement the handler:
```java
private CallToolResult handleMyTool(McpSyncServerExchange exchange, 
                                   Map<String, Object> args) {
    // Validate inputs
    // Execute logic
    // Return result
}
```

3. Register in server builder:
```java
.tool(createMyTool(), this::handleMyTool)
```

4. Add tests and documentation

## Release Process

1. Update version in `build.gradle`
2. Update CHANGELOG.md
3. Create release tag
4. Build release artifacts
5. Create GitHub release

## Getting Help

- Check existing issues and pull requests
- Read the MCP specification
- Ask questions in issues
- Join our community chat [if applicable]

## License

By contributing, you agree that your contributions will be licensed under the same license as the project.
