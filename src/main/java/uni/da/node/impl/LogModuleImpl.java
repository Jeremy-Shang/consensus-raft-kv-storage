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
    public int getPrevLogIndex() {
        return 0;
    }

    @Override
    public int getPrevLogTerm() {
        return 0;
    }

    @Override
    public LogEntry[] getLogEntries() {
        return new LogEntry[0];
    }

    @Override
    public LogEntry[] getEmptyLogEntries() {
        return new LogEntry[0];
    }

    @Override
    public int getMaxCommit() {
        return 0;
    }
}
