package uni.da.node;

import uni.da.entity.Log.LogEntry;

public interface LogModule extends RaftModule{

    public int getLastLogIndex();

    public int getLastLogTerm();

    public int prevLogIndex();

    public int prevLogTerm();

    public LogEntry[] getLogEntry();

    public int getMaxCommit();
}
