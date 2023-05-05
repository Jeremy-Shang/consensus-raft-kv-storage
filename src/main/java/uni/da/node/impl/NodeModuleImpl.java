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

    private final String remoteServiceName = "RaftRpcService";

    CountDownLatch latch;

    private NodeModuleImpl(ConsensusState consensusState) throws IOException {

        this.consensusState = consensusState;


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


        /** Rpc service registry and gather*/
        latch = new CountDownLatch(memberNum);
        remoteRegistry();
        latch.await();


        /** Start server state transfer*/
        Thread stateMachine = new Thread(new ServerStateTransfer(consensusState));
        stateMachine.start();


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
     *  Remote service
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
        registry.rebind(remoteServiceName, raftRpcService);

        log.info("[REMOTE REGISTRY] node{}: Local remote service registration success", consensusState.getId());

    }

    private void remoteClientRegistry() throws InterruptedException, RemoteException, NotBoundException {

        Map<Integer, Addr> clusterAddr = consensusState.getClusterAddr();
        Map<Integer, RaftRpcService> remoteServiceMap = new HashMap<>();

        for(Integer id: clusterAddr.keySet()) {
            Addr addr = clusterAddr.get(id);

            for(int count=1; ; count++){
                try {

                    String host = addr.getIp();
                    int port = addr.getPort();


                    Registry registry = LocateRegistry.getRegistry(host, port);
                    RaftRpcService remoteObject = (RaftRpcService) registry.lookup(remoteServiceName);

                    remoteServiceMap.put(id, remoteObject);

                    latch.countDown();

                    log.info("[REMOTE GATHER SUCCESS] Get remote service {}. ", addr.toString());

                    break;
                } catch (Exception e) {
                    Thread.sleep(3000);
                    e.printStackTrace();
                    log.info("[REMOTE GATHER FAIL] Get remote service {} fail. Retry times: {}", addr.toString(), count);
                }
            }
        }
        consensusState.setRemoteServiceMap(remoteServiceMap);
    }

}
