package uni.da.entity;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;


@Builder
@ToString
@Data
public class AppendEntryResponse implements Serializable {

    // currentTerm, for leader to update itself
    int term;

    // true if follower contained entry matching prevLogIndex and prevLogTerm
    boolean success;

    // TOOD: May useless
    int matchIndex;

    boolean isHeartBeat;

}
