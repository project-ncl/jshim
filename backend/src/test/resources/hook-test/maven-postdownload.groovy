println("${TOOL_NAME} ${TOOL_VERSION} ${TOOL_HOME}")

new File("/tmp/jshim-hook-test").withWriter { writer ->
    writer.writeLine "${TOOL_NAME}"
    writer.writeLine "${TOOL_VERSION}"
    writer.writeLine "${TOOL_HOME}"
}
