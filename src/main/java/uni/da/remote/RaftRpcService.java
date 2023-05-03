package uni.da.remote;

import uni.da.entity.*;
import uni.da.node.ConsensusState;

import java.util.concurrent.ExecutionException;


/*
    每个Raft节点实际提供的服务
 */
public interface RaftRpcService {

    public RequestVoteResponse requestVote(RequestVoteRequest request);

    public AppendEntryResponse appendEntry(AppendEntryRequest request);

    public void sayHi(ConsensusState config);

    public ClientResponse handleClient(ClientRequest request) throws ExecutionException, InterruptedException;
}
