package uni.da.node.impl;

import lombok.Data;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import uni.da.common.RedisDb;
import uni.da.entity.Log.LogBody;
import uni.da.entity.Log.LogEntry;
import uni.da.node.LogModule;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Data
@Slf4j
public class LogModuleImpl implements LogModule {

    String nodeId;

    String name;

    private CopyOnWriteArrayList<LogEntry> logEntries;

    public LogModuleImpl(String id) {
        this.nodeId = id;
        this.name = this.nodeId + "-logs";

//        Jedis jedis = RedisDb.getJedis();
//        if (jedis.exists(name)) {
//            logEntries = (CopyOnWriteArrayList<LogEntry>) RedisDb.getJsonObject(name, CopyOnWriteArrayList.class);
//        } else {
//            // first Index is 1. Index 0 contains fake data
//            /**
//             * "first index is 1"
//             * Index 0: placeholder data
//             */
//            logEntries = new CopyOnWriteArrayList<>(new LogEntry[]{
//                    new LogEntry(0, 0, new LogBody(9999, "placeholder 2")),
//            });
//        }

        logEntries = new CopyOnWriteArrayList<>(new LogEntry[]{
                new LogEntry(0, 0, new LogBody(0, "placeholder")),
        });
    }

    @Override
    public synchronized LogEntry getLogEntry(int index, int term) {

        for(LogEntry logEntry: logEntries) {
            if (logEntry.getLogIndex() == index && logEntry.getTerm() == term) {
                return logEntry;
            }
        }
        return null;
    }

    @Override
    public LogEntry getEntryByIndex(int index) {

        for (LogEntry logEntry: logEntries) {
            if (logEntry.getLogIndex() == index) {
                return logEntry;
            }
        }

        return null;
    }

    @Override
    public int getLastLogIndex() {

        List<Integer> indexes = logEntries.stream().map(e -> e.getLogIndex()).collect(Collectors.toList());


        return Collections.max(indexes);
    }

    @Override
    public int getLastLogTerm() {

        return logEntries.get(getLastLogIndex()).getTerm();
    }

    @Override
    public synchronized void append(LogEntry logEntry) {
        logEntries.add(logEntry);

        RedisDb.setJsonString(name, logEntries);
    }

    @Override
    public boolean contains(LogEntry entry) {
        return getLogEntry(entry.getLogIndex(), entry.getTerm()) != null ? true: false;
    }


    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    public static void main(String[] args) {
        LogModuleImpl logModule = new LogModuleImpl("1");

        log.info(logModule.getEntryByIndex(0).toString());
    }

}
