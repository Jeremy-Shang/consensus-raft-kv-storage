package uni.da.remote.impl;

import uni.da.remote.RaftRpcClient;
import uni.da.entity.AppendEntryRequest;
import uni.da.entity.RequestVoteRequest;
import uni.da.entity.AppendEntryResponse;
import uni.da.entity.RequestVoteResponse;

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
