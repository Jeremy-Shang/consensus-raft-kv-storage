package uni.da.entity;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import uni.da.entity.Log.LogEntry;

@Builder
@ToString
@Data
public class AppendEntryRequest {

    int term;

    int leaderId;

    int prevLogIndex;

    int preLogTerm;

    LogEntry[] logEntries;

    int leaderCommit;

}
