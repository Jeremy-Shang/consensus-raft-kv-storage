package uni.da.node.impl;

import lombok.Data;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uni.da.entity.Log.Command;
import uni.da.entity.Log.LogEntry;
import uni.da.node.LogModule;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


@Data
@RequiredArgsConstructor
public class LogModuleImpl implements LogModule {

    @NonNull private int maximumSize;

    // 日志队列
    private CopyOnWriteArrayList<LogEntry> logEntries = new CopyOnWriteArrayList<>(new LogEntry[]{
            new LogEntry(1, 0, new Command(-1, "fake"))
    });
    
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
        if (index >= logEntries.size()) {
            return null;
        }

        return this.logEntries.stream()
                .filter(e -> e.getLogIndex() == index)
                .collect(Collectors.toList())
                .get(0);
    }

    @Override
    public void commit(int index) {

    }

    @Override
    public int getLastLogIndex() {

        return logEntries.stream().map(e -> e.getLogIndex())
                .max((a, b) -> b - a).get();
    }

    @Override
    public int getLastLogTerm() {

        if (logEntries.size() == 0) {
            return 1;
        }

        return logEntries.get(getLastLogIndex()).getTerm();
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
        // TODO
        return 0;
    }

    @Override
    public boolean isPresent(int index) {
        if (index == 0) {
            return true;
        }

        return logEntries.stream().filter(e -> e.getLogIndex() == index).count() > 0;
    }

    @Override
    public void append(LogEntry logEntry) {

    }


}
