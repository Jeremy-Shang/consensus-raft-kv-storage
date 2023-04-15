package uni.da.node.impl;

import lombok.Data;

import lombok.extern.slf4j.Slf4j;

import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import uni.da.common.NodeConfig;
import uni.da.node.ConsensusModule;
import uni.da.node.LogModule;
import uni.da.node.Node;
import uni.da.node.StateMachineModule;
import uni.da.remote.RaftRpcService;
import uni.da.remote.impl.RaftRpcServiceImpl;
import uni.da.status.Status;

/*
    Raft 集群节点实例
        - 每次启动节点，启动对应的线程实例
        - 为了方便测试，暂时不单例
 */

@Slf4j
@Data
public class NodeImpl implements Node {

// 单例节点
//    private static NodeImpl nodeImpl = null;

    // 默认超时时间 TODO 配置化
    private int timeout = 500;

    // 节点配置信息
    private NodeConfig nodeConfig;

    // 集群节点状态默认 Follower
    private Status status = Status.Follower;

    // Raft节点定义模块
    private ConsensusModule consensusModule;

    private LogModule logModule;

    private StateMachineModule stateMachine;

    // 节点单例保证安全
    public NodeImpl(NodeConfig config) {
        this.nodeConfig = config;
    }


//    private static synchronized NodeImpl getInstance(NodeConfig config) {
//        if (NodeImpl.nodeImpl == null) {
//            NodeImpl.nodeImpl = new NodeImpl(config);
//        }
//
//        return NodeImpl.nodeImpl;
//    }


    public void start() {
        log.info("Node[{}] start at {}.", status, nodeConfig.getAddr());

        // TODO: 1. 启动心跳监听线程 2. 启动RPC监听 3.?
        // 注册该节点提供的RPC服务
        ServiceConfig<RaftRpcService> service = new ServiceConfig<>();
        service.setInterface(RaftRpcService.class);
        service.setRef(new RaftRpcServiceImpl());

        // 启动服务
        DubboBootstrap.getInstance()
                .application(nodeConfig.getName())
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .protocol(new ProtocolConfig("dubbo", nodeConfig.getPort()))
                .service(service)
                .start()            // 启动Dubbo
                .await();           // 挂起等待
    }

    @Override
    public void stop() {

    }


    @Override
    public void election() {

    }

    @Override
    public void heartBeat() {

    }

    @Override
    public void put(Object key, Object value) {

    }

    @Override
    public void get(Object key) {

    }

    public static void main(String[] args) {

        Node node = new NodeImpl(new NodeConfig());

    }
}
