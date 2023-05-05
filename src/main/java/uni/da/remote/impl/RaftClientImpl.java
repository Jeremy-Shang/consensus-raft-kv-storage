package uni.da.remote.impl;

import lombok.extern.slf4j.Slf4j;
import uni.da.common.Addr;
import uni.da.entity.ClientRequest;
import uni.da.entity.ClientResponse;
import uni.da.entity.Log.LogEntry;
import uni.da.remote.RaftClient;
import uni.da.remote.RaftRpcService;
import uni.da.util.LogUtil;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;


@Slf4j
public class RaftClientImpl implements RaftClient {

    Map<Integer, Addr> clusterAddr = new HashMap<>();

    Map<Integer, RaftRpcService> remoteServiceMap = new HashMap<>();

    CountDownLatch countDownLatch;


    public RaftClientImpl() throws InterruptedException, RemoteException {
        String ip = "127.0.0.1";
        clusterAddr.put(1, new Addr(ip, 6666));
        clusterAddr.put(2, new Addr(ip, 6667));
        clusterAddr.put(3, new Addr(ip, 6668));
//        clusterAddr.put(4, new Addr(ip, 6669));

        registry();

        remoteServiceMap.get(1).sayHi();

        countDownLatch.await();
    }


    /**
     * Client prompt
     *
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws RemoteException
     */
    @Override
    public void ClientPrompt() throws ExecutionException, InterruptedException, RemoteException {
        Scanner scanner = new Scanner(System.in);

        while (true) {

            String[] input = scanner.nextLine().split(" ");

            String command = input[0];

            if (command.equals("put")) {

                int key = Integer.parseInt(input[1]);
                String val = input[2];

                put(ClientRequest.builder()
                        .TYPE(0)
                        .key(key)
                        .val(val).build());
            } else if (command.equals("get")) {
                int key = scanner.nextInt();
                get(ClientRequest.builder()
                        .TYPE(1)
                        .key(key).build());
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
    public ClientResponse put(ClientRequest request) throws ExecutionException, InterruptedException, RemoteException {

        // TODO: client can randomly pick one node
        RaftRpcService service = remoteServiceMap.get(1);


        service.sayHi();


        ClientResponse<Map<Integer, List<LogEntry>>> response = service.handleClient(request);

        Map<Integer, List<LogEntry>> data = response.getData();

//         3. 打印数据

        LogUtil.printTable(data);

        return null;
    }

    @Override
    public ClientResponse get(ClientRequest request) {

        return null;

    }


    public void registry () throws InterruptedException {
        countDownLatch = new CountDownLatch(clusterAddr.size());

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
    }

}
