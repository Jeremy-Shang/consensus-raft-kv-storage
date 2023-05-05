package uni.da.remote;

import uni.da.entity.ClientRequest;
import uni.da.entity.ClientResponse;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;

public interface RaftClient {

    public ClientResponse put(ClientRequest request) throws ExecutionException, InterruptedException, RemoteException;


    public void ClientPrompt() throws ExecutionException, InterruptedException, RemoteException;


    public ClientResponse get(ClientRequest request);
}
