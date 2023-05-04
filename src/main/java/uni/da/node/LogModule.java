package uni.da.node;

import uni.da.entity.Log.LogEntry;

import java.util.concurrent.CopyOnWriteArrayList;

public interface LogModule extends RaftModule{

    public int getLastLogIndex();

    public int getLastLogTerm();

    public int getPrevLogIndex();

    public int getPrevLogTerm();

    public LogEntry getEmptyLogEntry();

    public int getMaxCommit();

    public boolean isPresent(int index);

    public void append(LogEntry logEntry);

    public CopyOnWriteArrayList<LogEntry> getLogEntries();

    public LogEntry getEntryByIndex(int index);

    public void apply(int index);

    public LogEntry getLogEntry(int index, int term);


}
