package uni.da.remote;

import uni.da.entity.ClientRequest;
import uni.da.entity.ClientResponse;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;

public interface RaftClient {

    public void put(ClientRequest request) throws ExecutionException, InterruptedException, RemoteException;


    public void prompt() throws ExecutionException, InterruptedException, RemoteException;


    public ClientResponse get(ClientRequest request);
}


