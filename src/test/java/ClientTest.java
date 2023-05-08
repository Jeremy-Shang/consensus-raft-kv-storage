import uni.da.entity.ClientRequest;
import uni.da.remote.RaftClient;
import uni.da.remote.impl.RaftClientImpl;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;

public class ClientTest {

    public static void main(String[] args) throws InterruptedException, ExecutionException, RemoteException, NotBoundException {
        RaftClient raftClient = new RaftClientImpl();

        raftClient.prompt();
    }
}
