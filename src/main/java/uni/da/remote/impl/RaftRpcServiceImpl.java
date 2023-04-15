package uni.da.remote.impl;

import uni.da.remote.RaftRpcService;

public class RaftRpcServiceImpl implements RaftRpcService {
    @Override
    public String requestVote(String name) {
        return "hi " + name;
    }

    @Override
    public String appendLog() {
        return null;
    }

    @Override
    public String heartBeat() {
        return null;
    }
}
