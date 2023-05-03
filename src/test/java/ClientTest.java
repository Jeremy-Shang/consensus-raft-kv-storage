import uni.da.remote.RaftClient;
import uni.da.remote.impl.RaftClientImpl;

import java.util.concurrent.ExecutionException;

public class ClientTest {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        RaftClient raftClient = new RaftClientImpl();

        raftClient.ClientPrompt();

    }
}
