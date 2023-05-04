package uni.da.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@AllArgsConstructor
@Builder
@ToString
public class RequestVoteRequest implements Serializable {

    // candidate’s term
    int term;

    // candidate that requesting vote
    int candidateId;

    // index of candidate’s last log entry
    int lastLogIndex;

    // term of candidate’s last log entry
    int lastLogTerm;
}
