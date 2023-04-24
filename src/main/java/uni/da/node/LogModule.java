package uni.da.node;

import uni.da.entity.Log.LogEntry;

public interface LogModule extends RaftModule{

    public int getLastLogIndex();

    public int getLastLogTerm();

    public int getPrevLogIndex();

    public int getPrevLogTerm();

    public LogEntry[] getLogEntries();

    public LogEntry[] getEmptyLogEntries();

    public int getMaxCommit();
}
