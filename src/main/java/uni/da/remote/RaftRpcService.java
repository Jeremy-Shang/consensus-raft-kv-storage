package uni.da.remote;

import uni.da.entity.AppendEntryRequest;
import uni.da.entity.RequestVoteRequest;
import uni.da.entity.AppendEntryResponse;
import uni.da.entity.RequestVoteResponse;

/*
    每个Raft节点实际提供的服务
 */
public interface RaftRpcService {

    public RequestVoteResponse handleRequestVote(RequestVoteRequest request);

    public AppendEntryResponse handleAppendEntry(AppendEntryRequest request);

    public void handleHeartBeat();

    public String hello();
}
