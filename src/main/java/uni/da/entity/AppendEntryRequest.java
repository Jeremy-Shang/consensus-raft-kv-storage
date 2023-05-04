package uni.da.entity;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import uni.da.entity.Log.LogEntry;

import java.io.Serializable;

@Builder
@ToString
@Data
public class AppendEntryRequest implements Serializable {

    // leader’s term
    int term;

    // so follower can redirect clients
    int leaderId;

    // index of log entry immediately preceding new ones
    int prevLogIndex;

    // term of prevLogIndex entry
    int preLogTerm;

    // TODO: log entries to store empty for heartBeat (currently  null for hearbeat)
    LogEntry logEntry;

    // leader’s commitIndex
    int leaderCommit;

}
