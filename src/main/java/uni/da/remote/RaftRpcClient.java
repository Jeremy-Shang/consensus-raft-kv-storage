package uni.da.remote;


import uni.da.entity.AppendEntryRequest;
import uni.da.entity.RequestVoteRequest;
import uni.da.entity.AppendEntryResponse;
import uni.da.entity.RequestVoteResponse;

/*
    每个Raft节点实际需要的服务
 */
public interface RaftRpcClient {

    public RequestVoteResponse requestVote(RequestVoteRequest request);

    public AppendEntryResponse appendEntry(AppendEntryRequest request);

    public void heartBeat();

    public String hello();
}
