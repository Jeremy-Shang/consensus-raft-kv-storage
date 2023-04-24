package uni.da.entity;

import lombok.Builder;
import lombok.Data;
import uni.da.entity.Log.LogEntry;

@Builder
@Data
public class AppendEntryRequest {

    int term;

    int leaderId;

    int prevLogIndex;

    int preLogTerm;

    LogEntry[] logEntries;

    int leaderCommit;

}
