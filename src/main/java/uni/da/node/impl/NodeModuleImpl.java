package uni.da.node.impl;

import lombok.Data;

import lombok.extern.slf4j.Slf4j;

import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;

import uni.da.common.Addr;
import uni.da.node.ConsensusState;
import uni.da.node.LogModule;
import uni.da.node.Node;
import java.rmi.registry.Registry;
import uni.da.remote.RaftRpcService;
import uni.da.remote.impl.RaftRpcServiceImpl;
import uni.da.statetransfer.ServerStateTransfer;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


/**
 *  Raft Node Instance starter.
 */
@Slf4j
@Data
public class NodeModuleImpl implements Node {

    private static NodeModuleImpl nodeImpl = null;

    private ConsensusState consensusState;

    private LogModule logModule;

    CountDownLatch countDownLatch;



    /**
     * 单例构造函数
     * @param consensusState
     * @throws IOException
     */
    private NodeModuleImpl(ConsensusState consensusState) throws IOException {

        this.consensusState = consensusState;

        /** 设置日志体，恢复当前任期号码*/
        this.consensusState.setLogModule(new LogModuleImpl(String.valueOf(consensusState.getId())));


        log.info("size" + String.valueOf(consensusState.getLogModule().getLogEntries().size()));
    }

    public static synchronized NodeModuleImpl getInstance(ConsensusState consensusState) throws IOException {
        if (NodeModuleImpl.nodeImpl == null) {
            NodeModuleImpl.nodeImpl = new NodeModuleImpl(consensusState);
        }
        return NodeModuleImpl.nodeImpl;
    }




    /**
     *
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
        Thread stateMachine = new Thread(new ServerStateTransfer(consensusState));
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
    private void remoteRegistry() throws InterruptedException {
        Thread client = new Thread(() -> {
            try {
                remoteClientRegistry();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (NotBoundException e) {
                throw new RuntimeException(e);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
        Thread server = new Thread(() -> {
            try {
                remoteServiceRegistry();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });

        server.start();
        Thread.sleep(1000);
        client.start();

    }

    private void remoteServiceRegistry() throws RemoteException {

        RaftRpcService raftRpcService = new RaftRpcServiceImpl(consensusState);

        Registry registry = LocateRegistry.createRegistry(consensusState.getAddr().getPort());
        registry.rebind("RaftRpcService", raftRpcService);

        log.info("注册本地节点服务成功");

    }

    private void remoteClientRegistry() throws InterruptedException, RemoteException, NotBoundException {
        log.info("加载集群远程服务...");

        Map<Integer, Addr> clusterAddr = consensusState.getClusterAddr();
        Map<Integer, RaftRpcService> remoteServiceMap = new HashMap<>();

        for(Integer id: clusterAddr.keySet()) {
            Addr addr = clusterAddr.get(id);

            for(int count=1; ; count++){
                try {
                    // 获取远程节点提供服务接口

                    String host = addr.getIp();
                    int port = addr.getPort();
                    String name = "RaftRpcService";

                    Registry registry = LocateRegistry.getRegistry(host, port);
                    RaftRpcService remoteObject = (RaftRpcService) registry.lookup(name);

                    // 保存在 id: service 中
                    remoteServiceMap.put(id, remoteObject);

                    countDownLatch.countDown();

                    log.info("获取远程服务成功: {}", addr.toString());

                    break;
                } catch (Exception e) {
                    Thread.sleep(3000);
                    e.printStackTrace();
                    log.info("获取远程服务失败: {} 尝试次数: {}", addr.toString(), count);
                }
            }
        }
        consensusState.setRemoteServiceMap(remoteServiceMap);
    }

}
