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
public class RequestVoteResponse implements Serializable {

    // currentTerm, for candidate to update itself
    int term;

    // true means candidate received vote
    boolean voteGranted;
}
