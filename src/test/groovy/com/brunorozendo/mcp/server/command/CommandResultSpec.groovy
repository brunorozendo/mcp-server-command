package com.brunorozendo.mcp.server.command

import spock.lang.Specification
import spock.lang.Unroll

class CommandResultSpec extends Specification {

    def "test default constructor initializes fields properly"() {
        when: "creating a new CommandResult"
        def result = new CommandResult()

        then: "all fields should be initialized with defaults"
        result.stdout == ""
        result.stderr == ""
        result.message == null
        !result.isError()
    }

    def "test getters and setters for stdout"() {
        given: "a new CommandResult"
        def result = new CommandResult()

        when: "setting stdout to a value"
        result.setStdout("test output")

        then: "stdout should be set correctly"
        result.getStdout() == "test output"

        when: "setting stdout to null"
        result.setStdout(null)

        then: "stdout should be empty string"
        result.getStdout() == ""
    }

    def "test getters and setters for stderr"() {
        given: "a new CommandResult"
        def result = new CommandResult()

        when: "setting stderr to a value"
        result.setStderr("error output")

        then: "stderr should be set correctly"
        result.getStderr() == "error output"

        when: "setting stderr to null"
        result.setStderr(null)

        then: "stderr should be empty string"
        result.getStderr() == ""
    }

    def "test getters and setters for message"() {
        given: "a new CommandResult"
        def result = new CommandResult()

        when: "setting message to a value"
        result.setMessage("test message")

        then: "message should be set correctly"
        result.getMessage() == "test message"

        when: "setting message to null"
        result.setMessage(null)

        then: "message should be null"
        result.getMessage() == null
    }

    def "test getters and setters for isError"() {
        given: "a new CommandResult"
        def result = new CommandResult()

        when: "setting error to true"
        result.setError(true)

        then: "isError should return true"
        result.isError()

        when: "setting error to false"
        result.setError(false)

        then: "isError should return false"
        !result.isError()
    }

    @Unroll
    def "test toString() method with various field combinations: #description"() {
        given: "a CommandResult with specific field values"
        def result = new CommandResult()
        result.setStdout(stdout)
        result.setStderr(stderr)
        result.setMessage(message)
        result.setError(isError)

        when: "calling toString()"
        def str = result.toString()

        then: "string representation should contain all fields"
        str.contains("stdout='$expectedStdout'")
        str.contains("stderr='$expectedStderr'")
        str.contains("message='$expectedMessage'")
        str.contains("isError=$isError")
        str.startsWith("CommandResult{")
        str.endsWith("}")

        where:
        description              | stdout      | stderr      | message      | isError || expectedStdout | expectedStderr | expectedMessage
        "all fields set"         | "out"       | "err"       | "msg"        | true    || "out"          | "err"          | "msg"
        "null values"            | null        | null        | null         | false   || ""             | ""             | "null"
        "empty strings"          | ""          | ""          | ""           | false   || ""             | ""             | ""
        "mixed values"           | "output"    | null        | "error msg"  | true    || "output"       | ""             | "error msg"
    }

    def "test defensive copying - stdout changes don't affect internal state"() {
        given: "a mutable string builder"
        def result = new CommandResult()
        def mutableString = new StringBuilder("initial")

        when: "setting stdout from the string builder"
        result.setStdout(mutableString.toString())
        mutableString.append(" modified")

        then: "stdout should not be affected by string builder changes"
        result.getStdout() == "initial"
    }
    
    def "test toString method directly"() {
        given: "a CommandResult with all fields set"
        def result = new CommandResult()
        result.setStdout("test output")
        result.setStderr("test error")
        result.setMessage("test message")
        result.setError(true)
        
        when: "calling toString()"
        def str = result.toString()
        
        then: "string should match expected format"
        str == "CommandResult{stdout='test output', stderr='test error', message='test message', isError=true}"
    }
    
    def "test toString with minimal fields"() {
        given: "a new CommandResult with defaults"
        def result = new CommandResult()
        
        when: "calling toString()"
        def str = result.toString()
        
        then: "string should show default values"
        str == "CommandResult{stdout='', stderr='', message='null', isError=false}"
    }
}
