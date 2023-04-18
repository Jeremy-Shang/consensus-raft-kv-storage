package uni.da.remote;

import uni.da.remote.request.AppendEntryRequest;
import uni.da.remote.request.RequestVoteRequest;
import uni.da.remote.respond.AppendEntryResponse;
import uni.da.remote.respond.RequestVoteResponse;

/*
    每个Raft节点实际提供的服务
 */
public interface RaftRpcService {

    public RequestVoteResponse handleRequestVote(RequestVoteRequest request);

    public AppendEntryResponse handleAppendEntry(AppendEntryRequest request);

    public void handleHeartBeat();
}
