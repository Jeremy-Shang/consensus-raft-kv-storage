package uni.da.node.impl;

import lombok.Data;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import uni.da.entity.Log.LogBody;
import uni.da.entity.Log.LogEntry;
import uni.da.node.LogModule;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


@Data
@RequiredArgsConstructor
public class LogModuleImpl implements LogModule {

    @NonNull private int maximumSize;

    // first Index is 1. Index 0 contains fake data
    private CopyOnWriteArrayList<LogEntry> logEntries = new CopyOnWriteArrayList<>(new LogEntry[]{
            new LogEntry(1, 0, new LogBody(-1, "fake"))
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
    public LogEntry getLogEntry(int index, int term) {

        List<LogEntry> lst = logEntries.stream()
                .filter(e -> e.getLogIndex() == index && e.getTerm() == term)
                .collect(Collectors.toList());

        return lst.size() == 0 ? null : lst.get(0);
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
    public void apply(int index) {
        // TODO applu to state machine
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
    public synchronized void append(LogEntry logEntry) {

    }


}
