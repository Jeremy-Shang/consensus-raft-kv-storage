package uni.da.node.impl;

import lombok.Data;

import lombok.extern.slf4j.Slf4j;

import org.apache.dubbo.config.*;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import uni.da.common.NodeConfig;
import uni.da.node.ConsensusModule;
import uni.da.node.LogModule;
import uni.da.node.Node;
import uni.da.node.StateMachineModule;

import uni.da.remote.RaftRpcService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

/*
    Raft 集群节点实例
        - 每次启动节点，启动对应的线程实例
 */
@Slf4j
@Data
public class NodeImpl implements Node {

    enum Character {
        Follower, Candidate, Leader

    }

    // 单例节点
    private static NodeImpl nodeImpl = null;

    // 节点配置信息
    private NodeConfig nodeConfig;

    // 集群节点状态默认 Follower
    private Character status = Character.Follower;

    // Raft节点定义模块
    private ConsensusModule consensusModule;

    private LogModule logModule;

    private StateMachineModule stateMachine;

    // Raft节点线程池
    private ExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());



    // 节点单例保证安全
    private NodeImpl(NodeConfig config) throws IOException {
        // 设置节点属性
        this.nodeConfig = config;
        nodeConfig.setUuid(String.valueOf(UUID.randomUUID()));

        // 初始化模块
        consensusModule = ConsensusModuleImpl.getInstance();
    }

    public static synchronized NodeImpl getInstance(NodeConfig config) throws IOException {
        if (NodeImpl.nodeImpl == null) {
            NodeImpl.nodeImpl = new NodeImpl(config);
        }

        return NodeImpl.nodeImpl;
    }

    public void start() throws InterruptedException {
        log.info("Node[{}] start at {}:{}.", status, nodeConfig.getIp(), nodeConfig.getPort());
        // TODO: 1. 启动心跳监听线程 2. 启动RPC监听 3.?

        // 1. 服务注册
        registry();

        // 2.

        log.info(consensusModule.sayHi());

        // 主线程阻塞
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (this) {
                this.notifyAll();
            }
        }));

        synchronized (this) {
            this.wait();
        }
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

    /*
        异步任务定义
     */












    /*
        RPC 加载
     */

    private void registry() {
        Thread client = new Thread(() -> remoteClientRegistry());
        Thread server = new Thread(() -> remoteServiceRegistry());

        client.start();
        server.start();
    }

    private void remoteServiceRegistry() {
        // 服务提供者暴露服务配置
        ServiceConfig<RaftRpcService> service = new ServiceConfig<RaftRpcService>();
        service.setInterface(RaftRpcService.class);
        service.setRef(consensusModule.getSelfRpcService());
        service.setTimeout(nodeConfig.getTimeout());

        // 启动 Dubbo
        DubboBootstrap.getInstance()
                .application("first-dubbo-provider")
                .registry(new RegistryConfig("N/A"))
                .protocol(new ProtocolConfig("dubbo", nodeConfig.getPort()))
                .service(service)
                .start()
                .await();
    }

    private void remoteClientRegistry(){
        log.info("加载集群远程服务...");
        System.out.println("加载远程服务");
        List<NodeConfig> nodeConfigList = nodeConfig.getClusterConfig();
        Map<String, RaftRpcService> remoteServiceMap = new HashMap<>();

        for(NodeConfig config: nodeConfigList) {
            // 连接注册中心配置 (不使用)
            RegistryConfig registry = new RegistryConfig();
            registry.setAddress("N/A");

            // 引用远程服务 reference 为重对象
            ReferenceConfig<RaftRpcService> reference = new ReferenceConfig<RaftRpcService>();
            reference.setRegistry(registry);
            reference.setInterface(RaftRpcService.class);
            reference.setUrl("dubbo://" + config.getIp() + ":" + config.getPort());
            reference.setTimeout(config.getTimeout());

            try {
                RaftRpcService raftRpcService = reference.get();
                // 获取该节点对应的服务
                remoteServiceMap.put(config.getUuid(), raftRpcService);
            } catch (Exception e) {
                log.info("获取远程服务失败: {}", config.getName());
            }
        }

        consensusModule.setRemoteRpcServices(remoteServiceMap);
    }

}
