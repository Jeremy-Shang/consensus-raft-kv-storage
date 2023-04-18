package uni.da.remote.impl;

import uni.da.remote.RaftRpcService;
import uni.da.remote.request.AppendEntryRequest;
import uni.da.remote.request.RequestVoteRequest;
import uni.da.remote.respond.AppendEntryResponse;
import uni.da.remote.respond.RequestVoteResponse;

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
