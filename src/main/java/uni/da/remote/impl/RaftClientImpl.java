package uni.da.remote.impl;

import lombok.extern.slf4j.Slf4j;
import uni.da.common.Addr;
import uni.da.entity.ClientRequest;
import uni.da.entity.ClientResponse;
import uni.da.entity.Log.LogEntry;
import uni.da.remote.RaftClient;
import uni.da.remote.RaftRpcService;
import uni.da.util.LogType;
import uni.da.util.LogUtil;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;


@Slf4j
public class RaftClientImpl implements RaftClient {

    Map<Integer, Addr> clusterAddr = new HashMap<>();

    Map<Integer, RaftRpcService> remoteServiceMap = new HashMap<>();

    CountDownLatch latch;

    final String remoteServiceName = "RaftRpcService";

    public RaftClientImpl() throws InterruptedException, RemoteException {

        String ip = "127.0.0.1";

        clusterAddr.put(1, new Addr(ip, 6666));
        clusterAddr.put(2, new Addr(ip, 6667));
        clusterAddr.put(3, new Addr(ip, 6668));
        clusterAddr.put(4, new Addr(ip, 6669));
        clusterAddr.put(5, new Addr(ip, 6670));

        registry();

        latch.await();
    }


    /**
     * Client prompt
     *
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws RemoteException
     */
    @Override
    public void prompt() throws ExecutionException, InterruptedException, RemoteException {
        Scanner scanner = new Scanner(System.in);
        while (true) {

            String[] input = scanner.nextLine().split(" ");

            String command = input[0];

            if (command.equals("put")) {
                int key = Integer.parseInt(input[1]);
                String val = input[2];

                put(new ClientRequest(ClientRequest.Type.PUT, key, val));
            } else if (command.equals("get")) {
                int key = scanner.nextInt();

                get(new ClientRequest(ClientRequest.Type.GET, key));
            }
        }
    }


    /**
     * Put operation
     * @param request
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws RemoteException
     */
    @Override
    public void put(ClientRequest request) throws ExecutionException, InterruptedException, RemoteException {

        // TODO: client can randomly pick one node
        Random random = new Random();
        int random_id = random.nextInt(5) + 1;

        log.info("[CLIENT] send command to node {}", random_id);

        RaftRpcService service = remoteServiceMap.get(random_id);

        service.sayHi();

        ClientResponse<List<Map<Integer, List<LogEntry>>>> clientResponse = service.handleClient(request);

        if (clientResponse == null) {
            return ;
        }

        Map<Integer, List<LogEntry>> before = clientResponse.getData().get(0);
        Map<Integer, List<LogEntry>> after = clientResponse.getData().get(1);

        LogUtil.printTable(before);
        Thread.sleep(2000);
        LogUtil.printTable(after);
    }

    @Override
    public ClientResponse get(ClientRequest request) {

        return null;

    }


    public void registry () throws InterruptedException {
        latch = new CountDownLatch(clusterAddr.size());

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

                    log.info("[{}: gather success] Get remote service {}. ", LogType.REMOTE_RPC,  addr.toString());

                    break;
                } catch (Exception e) {
                    Thread.sleep(3000);
                    e.printStackTrace();
                    log.info("[{}: gather fail] Get remote service {} fail. Retry times: {}", LogType.REMOTE_RPC, addr.toString(), count);
                }
            }
        }
    }


    public static void main(String[] args) throws RemoteException, InterruptedException, ExecutionException {
        RaftClientImpl raftClient = new RaftClientImpl();

        raftClient.prompt();

    }

}
