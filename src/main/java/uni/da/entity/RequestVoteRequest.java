package uni.da.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@Builder
@ToString
public class RequestVoteRequest {
    int term;

    int candidateId;

    int lastLogIndex;

    int lastLogTerm;
}
