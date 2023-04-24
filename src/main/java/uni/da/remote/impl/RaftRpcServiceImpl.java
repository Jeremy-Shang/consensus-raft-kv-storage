package uni.da.remote.impl;

import uni.da.remote.RaftRpcService;
import uni.da.entity.AppendEntryRequest;
import uni.da.entity.RequestVoteRequest;
import uni.da.entity.AppendEntryResponse;
import uni.da.entity.RequestVoteResponse;

public class RaftRpcServiceImpl implements RaftRpcService {


    @Override
    public RequestVoteResponse handleRequestVote(RequestVoteRequest request) {
        return null;
    }

    @Override
    public AppendEntryResponse handleAppendEntry(AppendEntryRequest request) {
        return null;
    }

    @Override
    public void handleHeartBeat() {

    }

    @Override
    public String hello() {
        return "Hello world!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
    }
}
