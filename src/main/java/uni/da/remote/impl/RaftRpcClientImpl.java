package uni.da.remote.impl;

import uni.da.remote.RaftRpcClient;
import uni.da.remote.request.AppendEntryRequest;
import uni.da.remote.request.RequestVoteRequest;
import uni.da.remote.respond.AppendEntryResponse;
import uni.da.remote.respond.RequestVoteResponse;

public class RaftRpcClientImpl implements RaftRpcClient {

    @Override
    public RequestVoteResponse requestVote(RequestVoteRequest request) {
        return null;
    }

    @Override
    public AppendEntryResponse appendEntry(AppendEntryRequest request) {
        return null;
    }

    @Override
    public void heartBeat() {

    }

    @Override
    public String hello() {
        return null;
    }
}
