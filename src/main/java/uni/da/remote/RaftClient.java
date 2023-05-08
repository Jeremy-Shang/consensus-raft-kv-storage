package uni.da.remote;

import uni.da.entity.ClientRequest;
import uni.da.entity.ClientResponse;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;

public interface RaftClient {

    public void put(ClientRequest request) throws ExecutionException, InterruptedException, RemoteException, NotBoundException;


    public void prompt() throws ExecutionException, InterruptedException, RemoteException, NotBoundException;


    public void get(ClientRequest request) throws ExecutionException, RemoteException, InterruptedException, NotBoundException;
}


