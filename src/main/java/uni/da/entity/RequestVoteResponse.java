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
    int term;

    boolean isVote;
}
