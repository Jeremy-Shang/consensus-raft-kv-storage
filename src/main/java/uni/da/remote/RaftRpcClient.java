package uni.da.remote;


import uni.da.remote.request.AppendEntryRequest;
import uni.da.remote.request.RequestVoteRequest;
import uni.da.remote.respond.AppendEntryResponse;
import uni.da.remote.respond.RequestVoteResponse;

/*
    每个Raft节点实际需要的服务
 */
public interface RaftRpcClient {

    public RequestVoteResponse requestVote(RequestVoteRequest request);

    public AppendEntryResponse appendEntry(AppendEntryRequest request);

    public void heartBeat();
}
