package com.ledgerbridge.mcpclient;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AgentToolCallRecorder {

    private final ThreadLocal<List<ToolCallRecord>> currentCalls =
            ThreadLocal.withInitial(ArrayList::new);

    public void startRecording() {
        currentCalls.set(new ArrayList<>());
    }

    public void record(ToolCallRecord record) {
        currentCalls.get().add(record);
    }

    public List<ToolCallRecord> getRecordedCalls() {
        return List.copyOf(currentCalls.get());
    }

    public void stopRecording() {
        currentCalls.remove();
    }

    public record ToolCallRecord(
            String toolName,
            String arguments,
            String result,
            String error) {
    }
}
