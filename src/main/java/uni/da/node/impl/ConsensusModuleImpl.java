package uni.da.node.impl;

import lombok.extern.slf4j.Slf4j;

import uni.da.node.ConsensusModule;
import uni.da.remote.RaftRpcClient;
import uni.da.remote.RaftRpcService;
import uni.da.remote.impl.RaftRpcClientImpl;
import uni.da.remote.impl.RaftRpcServiceImpl;

import java.util.Map;


@Slf4j
public class ConsensusModuleImpl implements ConsensusModule {

    // 单例模块
    private static ConsensusModuleImpl consensusModuleImpl = null;

    private final RaftRpcService raftRpcService = new RaftRpcServiceImpl();

    private final RaftRpcClient raftRpcClient = new RaftRpcClientImpl();

    private Map<String, RaftRpcService> remoteServiceMap;

    private ConsensusModuleImpl() {

    }

    public static ConsensusModuleImpl getInstance() {
        if (consensusModuleImpl == null ) {
            consensusModuleImpl = new ConsensusModuleImpl();
        }
        return consensusModuleImpl;
    }

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
        return raftRpcService;
    }

    @Override
    public void setRemoteRpcServices(Map<String, RaftRpcService> remoteServiceMap) {
        this.remoteServiceMap = remoteServiceMap;
    }
}
