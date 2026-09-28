package com.ledgerbridge.mcpclient;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

public class RecordingToolCallback implements ToolCallback {

    private final ToolCallback delegate;
    private final AgentToolCallRecorder recorder;

    public RecordingToolCallback(
            ToolCallback delegate,
            AgentToolCallRecorder recorder) {
        this.delegate = delegate;
        this.recorder = recorder;
    }

    @Override
    public String call(String toolInput) {
        String toolName = delegate.getToolDefinition().name();

        try {
            String result = delegate.call(toolInput);

            recorder.record(
                    new AgentToolCallRecorder.ToolCallRecord(
                            toolName,
                            toolInput,
                            result,
                            null));

            return result;
        } catch (RuntimeException exception) {
            recorder.record(
                    new AgentToolCallRecorder.ToolCallRecord(
                            toolName,
                            toolInput,
                            null,
                            exception.getMessage()));

            throw exception;
        }
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
    }
}
