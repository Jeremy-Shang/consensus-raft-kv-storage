package uni.da.remote;

import uni.da.node.NodeParam;
import uni.da.entity.AppendEntryRequest;
import uni.da.entity.RequestVoteRequest;
import uni.da.entity.AppendEntryResponse;
import uni.da.entity.RequestVoteResponse;


/*
    每个Raft节点实际提供的服务
 */
public interface RaftRpcService {

    public RequestVoteResponse requestVote(RequestVoteRequest request);

    public AppendEntryResponse appendEntry(AppendEntryRequest request);

    public void sayHi(NodeParam config);
}
