package uni.da.node.impl;

import lombok.Data;

import redis.clients.jedis.Jedis;
import uni.da.common.RedisDb;
import uni.da.entity.Log.LogBody;
import uni.da.entity.Log.LogEntry;
import uni.da.node.LogModule;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Data
public class LogModuleImpl implements LogModule {

    String nodeId;

    String name;

    private CopyOnWriteArrayList<LogEntry> logEntries;

    public LogModuleImpl(String id) {
        this.nodeId = id;
        this.name = this.nodeId + "-logs";

        Jedis jedis = RedisDb.getJedis();
        if (jedis.exists(name)) {
            logEntries = (CopyOnWriteArrayList<LogEntry>) RedisDb.getJsonObject(name, CopyOnWriteArrayList.class);
        } else {
            // first Index is 1. Index 0 contains fake data
            logEntries = new CopyOnWriteArrayList<>(new LogEntry[]{
                    new LogEntry(0, 0, new LogBody(-1, "fake"))
            });
        }
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
    public synchronized void append(LogEntry logEntry) {
        logEntries.add(logEntry);

        RedisDb.setJsonString(name, logEntries);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

}
