package uni.da.remote.impl;

import lombok.extern.slf4j.Slf4j;
import uni.da.common.NodeParam;
import uni.da.remote.RaftRpcService;
import uni.da.entity.AppendEntryRequest;
import uni.da.entity.RequestVoteRequest;
import uni.da.entity.AppendEntryResponse;
import uni.da.entity.RequestVoteResponse;

@Slf4j
public class RaftRpcServiceImpl implements RaftRpcService {


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
    public void sayHi(NodeParam config) {
        log.warn(config.getName() + " say hi to you from " + config.getIp() + ":" + config.getPort());
    }
}
