package uni.da.node.impl;

import lombok.Data;

import lombok.extern.slf4j.Slf4j;

import org.apache.dubbo.config.*;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import uni.da.common.Addr;
import uni.da.node.ConsensusState;
import uni.da.node.LogModule;
import uni.da.node.Node;

import uni.da.remote.RaftRpcService;
import uni.da.remote.impl.RaftRpcServiceImpl;
import uni.da.statemachine.RaftStateMachine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/*
    Raft 集群节点实例
        - 每次启动节点，启动对应的线程实例
 */
@Slf4j
@Data
public class NodeImpl implements Node {


    // 单例节点
    private static NodeImpl nodeImpl = null;

    // 节点配置信息
    private ConsensusState consensusState;


    // Raft节点定义模块

    private LogModule logModule;


    // 通用同步工具
    CountDownLatch countDownLatch;


    /**
     * 单例构造函数
     * @param consensusState
     * @throws IOException
     */
    private NodeImpl(ConsensusState consensusState) throws IOException {

        this.consensusState = consensusState;

        /** 设置日志体，恢复当前任期号码*/

        this.consensusState.setLogModule(new LogModuleImpl(100));

        log.info("size" + String.valueOf(consensusState.getLogModule().getLogEntries().size()));
//        this.consensusState.getTerm().getAndSet(this.consensusState.getLogModule().getLastLogIndex());

    }

    public static synchronized NodeImpl getInstance(ConsensusState consensusState) throws IOException {
        if (NodeImpl.nodeImpl == null) {
            NodeImpl.nodeImpl = new NodeImpl(consensusState);
        }

        return NodeImpl.nodeImpl;
    }


    /**
     * 启动raft节点
     * @throws InterruptedException
     */
    public void start() throws InterruptedException, IOException {
        log.info("Node[{}] start at {}:{}.", consensusState.getCharacter() , consensusState.getAddr().getIp(), consensusState.getAddr().getPort());

        int memberNum = consensusState.getClusterAddr().size();
        /** RPC注册*/
        // 等待RPC集群接口注册完毕
        countDownLatch = new CountDownLatch(memberNum);

        remoteRegistry();

        // 阻塞, 直到集群注册完毕
        countDownLatch.await();








        /** 启动状态机流转*/
        Thread stateMachine = new Thread(new RaftStateMachine(consensusState));
        stateMachine.start();

        /** client */





        // 主线程阻塞
        stateMachine.join();

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
        Thread client = new Thread(() -> {
            try {
                remoteClientRegistry();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        Thread server = new Thread(() -> remoteServiceRegistry());

        client.start();
        server.start();
    }

    private void remoteServiceRegistry() {
        log.info("注册本地RPC服务");
        // 注册本节点的RPC服务
        ServiceConfig<RaftRpcService> service = new ServiceConfig<RaftRpcService>();
        service.setInterface(RaftRpcService.class);
        service.setRef(new RaftRpcServiceImpl(consensusState));
//        service.setTimeout();


        // 启动，暴露服务
        DubboBootstrap.getInstance()
                .application("first-dubbo-provider")
                .registry(new RegistryConfig("N/A"))
                .protocol(new ProtocolConfig("dubbo", consensusState.getAddr().getPort()))
                .service(service)
                .start()
                .await();
    }

    private void remoteClientRegistry() throws InterruptedException {
        log.info("加载集群远程服务...");

        Map<Integer, Addr> clusterAddr = consensusState.getClusterAddr();
        Map<Integer, RaftRpcService> remoteServiceMap = new HashMap<>();

        for(Integer id: clusterAddr.keySet()) {
            Addr addr = clusterAddr.get(id);
            // 连接注册中心配置 (不使用)
            RegistryConfig registry = new RegistryConfig();

            ReferenceConfig<RaftRpcService> reference = new ReferenceConfig<RaftRpcService>();
            reference.setInterface(RaftRpcService.class);
            reference.setUrl("dubbo://" + addr.getIp() + ":" + addr.getPort());

            for(int count=1; ; count++){
                try {
                    // 获取远程节点提供服务接口
                    RaftRpcService raftRpcService = reference.get();
                    // 保存在 id: service 中
                    remoteServiceMap.put(id, raftRpcService);

                    countDownLatch.countDown();
                    break;
                } catch (Exception e) {
                    Thread.sleep(3000);
                    log.info("获取远程服务失败: {} 尝试次数: {}", addr.toString(), count);
                }
            }
        }

        this.consensusState.setRemoteServiceMap(remoteServiceMap);

        log.info("远程服务获取成功");
    }

}
