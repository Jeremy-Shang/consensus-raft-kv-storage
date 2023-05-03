import uni.da.entity.ClientRequest;
import uni.da.remote.RaftClient;
import uni.da.remote.impl.RaftClientImpl;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;

public class ClientTest {

    public static void main(String[] args) throws InterruptedException, ExecutionException, RemoteException {
        RaftClient raftClient = new RaftClientImpl();

//        ClientRequest request = new ClientRequest(0, 1, "6666");
//
//        RaftClientImpl raftClient1 = new RaftClientImpl();
//        raftClient1.put(request);
        raftClient.ClientPrompt();
    }
}
