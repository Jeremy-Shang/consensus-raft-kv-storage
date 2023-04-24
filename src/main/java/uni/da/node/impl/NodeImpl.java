package uni.da.node.impl;

import lombok.Data;

import lombok.extern.slf4j.Slf4j;

import org.apache.dubbo.config.*;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import uni.da.common.Addr;
import uni.da.common.NodeParam;
import uni.da.node.ConsensusModule;
import uni.da.node.LogModule;
import uni.da.node.Node;
import uni.da.node.StateMachineModule;

import uni.da.remote.RaftRpcService;
import uni.da.statemachine.RaftStateMachine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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


    // 集群节点状态默认 Follower
    private Character status = Character.Follower;


    // 单例节点
    private static NodeImpl nodeImpl = null;

    // 节点配置信息
    private NodeParam nodeParam;


    // Raft节点定义模块
    private ConsensusModule consensusModule;

    private LogModule logModule;

    private StateMachineModule stateMachine;


    /**
     * 单例构造函数
     * @param nodeParam
     * @throws IOException
     */
    private NodeImpl(NodeParam nodeParam) throws IOException {

        this.nodeParam = nodeParam;

        /** 设置日志体，恢复当前任期号码*/

        this.nodeParam.setLogModule(new LogModuleImpl(100));

        this.nodeParam.getTerm().getAndSet(this.nodeParam.getLogModule().getLastLogIndex());

    }

    public static synchronized NodeImpl getInstance(NodeParam nodeParam) throws IOException {
        if (NodeImpl.nodeImpl == null) {
            NodeImpl.nodeImpl = new NodeImpl(nodeParam);
        }

        return NodeImpl.nodeImpl;
    }


    /**
     * 启动raft节点
     * @throws InterruptedException
     */
    public void start() throws InterruptedException, IOException {
        log.info("Node[{}] start at {}:{}.", status, nodeParam.getAddr().getIp(), nodeParam.getAddr().getPort());

        /** RPC注册*/
        remoteRegistry();

        /** 启动状态机流转*/
        Thread stateMachine = new Thread(new RaftStateMachine(nodeParam));
        stateMachine.start();






        // 3. 启动对客户端线程
        log.info(consensusModule.sayHi());



        /** 主线程阻塞*/
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
    public void put(Object key, Object value) {

    }

    @Override
    public void get(Object key) {

    }


    /**
     *  远程服务注册
     */
    private void remoteRegistry() {
        Thread client = new Thread(() -> remoteClientRegistry());
        Thread server = new Thread(() -> remoteServiceRegistry());

        client.start();
        server.start();
    }

    private void remoteServiceRegistry() {
        // 注册本节点的RPC服务
        ServiceConfig<RaftRpcService> service = new ServiceConfig<RaftRpcService>();
        service.setInterface(RaftRpcService.class);
        service.setRef(consensusModule.getSelfRpcService());
        service.setTimeout(nodeParam.getTimeout());

        // 启动，暴露服务
        DubboBootstrap.getInstance()
                .application("first-dubbo-provider")
                .registry(new RegistryConfig("N/A"))
                .protocol(new ProtocolConfig("dubbo", nodeParam.getAddr().getPort()))
                .service(service)
                .start()
                .await();
    }

    private void remoteClientRegistry(){
        log.info("加载集群远程服务...");
        System.out.println("加载远程服务");
        Map<Integer, Addr> clusterAddr = nodeParam.getClusterAddr();
        Map<Integer, RaftRpcService> remoteServiceMap = new HashMap<>();

        for(Integer id: clusterAddr.keySet()) {
            Addr addr = clusterAddr.get(id);
            // 连接注册中心配置 (不使用)
            RegistryConfig registry = new RegistryConfig();
            registry.setAddress("N/A");

            ReferenceConfig<RaftRpcService> reference = new ReferenceConfig<RaftRpcService>();
            reference.setRegistry(registry);
            reference.setInterface(RaftRpcService.class);
            reference.setUrl("dubbo://" + addr.getIp() + ":" + addr.getPort());

            try {
                // 获取远程节点提供服务接口
                RaftRpcService raftRpcService = reference.get();
                // 保存在 id: service 中
                remoteServiceMap.put(id, raftRpcService);
            } catch (Exception e) {
                log.info("获取远程服务失败: {}", addr.toString());
            }
        }

        this.nodeParam.setRemoteServiceMap(remoteServiceMap);
    }

}
