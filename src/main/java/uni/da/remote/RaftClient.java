package uni.da.remote;

import org.apache.dubbo.remoting.Client;
import uni.da.entity.ClientRequest;
import uni.da.entity.ClientResponse;

import java.util.concurrent.ExecutionException;

public interface RaftClient {


    public ClientResponse put(ClientRequest request) throws ExecutionException, InterruptedException;





    public ClientResponse get(ClientRequest request);
}
