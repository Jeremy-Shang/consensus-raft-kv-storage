package uni.da.node.impl;

import lombok.Data;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uni.da.entity.Log.LogEntry;
import uni.da.node.LogModule;


@Data
@RequiredArgsConstructor
public class LogModuleImpl implements LogModule {

    @NonNull private int maximumSize;

    private LogEntry[] logEntries;

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public int getLastLogIndex() {
        return 0;
    }

    @Override
    public int getLastLogTerm() {
        return 0;
    }

    @Override
    public int prevLogIndex() {
        return 0;
    }

    @Override
    public int prevLogTerm() {
        return 0;
    }

    @Override
    public LogEntry[] getLogEntry() {
        return new LogEntry[0];
    }

    @Override
    public int getMaxCommit() {
        return 0;
    }
}
