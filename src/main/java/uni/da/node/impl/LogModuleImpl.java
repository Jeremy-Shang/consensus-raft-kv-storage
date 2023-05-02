package uni.da.node.impl;

import lombok.Data;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uni.da.entity.Log.LogEntry;
import uni.da.node.LogModule;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


@Data
@RequiredArgsConstructor
public class LogModuleImpl implements LogModule {

    @NonNull private int maximumSize;


    // 日志队列
    private CopyOnWriteArrayList<LogEntry> logEntries;
    
    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public CopyOnWriteArrayList<LogEntry> getLogEntries() {
        return logEntries;
    }

    @Override
    public LogEntry getEntryByIndex(int index) {
        return this.logEntries.stream()
                .filter(e -> e.getLogIndex() == index)
                .collect(Collectors.toList())
                .get(0);
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
    public LogEntry getEmptyLogEntry() {
        return null;
    }

    @Override
    public int getMaxCommit() {
        return 0;
    }

    @Override
    public boolean isPresent(int index) {
        return false;
    }

    @Override
    public void append(LogEntry logEntry) {

    }


}
