package uni.da.node;

import uni.da.entity.Log.LogBody;
import uni.da.entity.Log.LogEntry;

import java.util.concurrent.CopyOnWriteArrayList;

public interface LogModule extends RaftModule{

    public int getLastLogIndex();

    public int getLastLogTerm();

    public void append(LogEntry logEntry);

    public CopyOnWriteArrayList<LogEntry> getLogEntries();

    public LogEntry getEntryByIndex(int index);


    public LogEntry getLogEntry(int index, int term);


}
