# Changelog

All notable changes to the MCP Command Server will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.5.0] - 2024-12-XX

### Added
- Initial release of MCP Command Server
- Core command execution functionality via `run_command` tool
- Support for working directory specification
- STDIN input support for piping data to commands
- 60-second timeout protection for all commands
- Special handling for Fish shell with STDIN
- Verbose logging mode with `--verbose` flag
- Cross-platform support (macOS, Linux, Windows)
- Thread-safe output stream handling
- Comprehensive error handling and reporting
- Example configuration for Claude Desktop integration
- Test script for validation

### Technical Details
- Built with Java 21
- Uses MCP SDK 0.10.0
- Implements Model Context Protocol specification
- JSON-RPC 2.0 communication over stdio
- SLF4J logging with simple implementation

### Security
- Commands execute with Java process user privileges
- No built-in sandboxing (security through user trust)
- Timeout protection prevents hanging processes

### Known Limitations
- Single command execution only (no command chaining)
- No background process management
- Memory-bound output buffering
- No streaming output support

## [Unreleased]

### Planned Features
- Command whitelisting/blacklisting capability
- Configurable timeout duration
- Streaming output for long-running commands
- Background process management
- Resource usage limits
- Audit logging
- Additional tool implementations
- Enhanced security options

### Under Consideration
- Docker containerization for isolation
- Multi-command transaction support
- Command history and replay
- Output formatting options
- Integration with other MCP tools

---

## Version Guidelines

### Version Numbering
- MAJOR version: Incompatible API changes
- MINOR version: Backwards-compatible functionality additions
- PATCH version: Backwards-compatible bug fixes

### Release Process
1. Update version in `build.gradle`
2. Update this CHANGELOG.md
3. Tag the release: `git tag -a v0.5.0 -m "Release version 0.5.0"`
4. Build release artifacts: `./gradlew clean build`
5. Create GitHub release with artifacts
