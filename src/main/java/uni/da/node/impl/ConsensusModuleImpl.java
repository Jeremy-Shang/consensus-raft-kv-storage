package uni.da.node.impl;

import lombok.extern.slf4j.Slf4j;

import uni.da.node.ConsensusModule;
import uni.da.remote.RaftRpcService;
import uni.da.remote.impl.RaftRpcServiceImpl;

import java.util.Map;


@Slf4j
public class ConsensusModuleImpl implements ConsensusModule {

    // 单例模块
    private static ConsensusModuleImpl consensusModuleImpl = null;

//    public RaftRpcService raftRpcService = new RaftRpcServiceImpl();


    public Map<String, RaftRpcService> remoteServiceMap;

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
    public RaftRpcService getSelfRpcService() {
//        return raftRpcService;
        return null;
    }

    @Override
    public void setRemoteRpcServices(Map<String, RaftRpcService> remoteServiceMap) {
        this.remoteServiceMap = remoteServiceMap;
    }

    @Override
    public String sayHi() {
//        StringBuilder sb = new StringBuilder();
//        remoteServiceMap.keySet().forEach(k -> sb.append(remoteServiceMap.get(k).hello()));
//
//        for(String k: remoteServiceMap.keySet()) {
//            log.info("在这里调用Hello服务");
//            String s = remoteServiceMap.get(k).hello();
//            sb.append(s);
//        }
//        return sb.toString();

        return null;
    }
}
