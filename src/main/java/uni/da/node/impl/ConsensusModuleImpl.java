package uni.da.node.impl;

import lombok.extern.slf4j.Slf4j;

import uni.da.node.ConsensusModule;
import uni.da.remote.RaftRpcClient;
import uni.da.remote.RaftRpcService;


@Slf4j
public class ConsensusModuleImpl implements ConsensusModule {


    @Override
    public void start() {
    }

    @Override
    public void stop() {

    }

    @Override
    public RaftRpcClient getRpcClient() {
        return null;
    }

    @Override
    public RaftRpcService getSelfRpcService() {
        return null;
    }

    @Override
    public void setRemoteRpcServices() {

    }
}
