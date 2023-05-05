package uni.da.remote;

import uni.da.entity.*;
import uni.da.entity.Log.LogEntry;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

/*
    每个Raft节点实际提供的服务
 */
public interface RaftRpcService extends Remote {

    public RequestVoteResponse requestVote(RequestVoteRequest request) throws RemoteException;

    public AppendEntryResponse appendEntry(AppendEntryRequest request) throws RemoteException;

    public void sayHi() throws RemoteException;

    public ClientResponse handleClient(ClientRequest request) throws ExecutionException, InterruptedException, RemoteException;

    public CopyOnWriteArrayList<LogEntry> gatherClusterLogEntries() throws RemoteException;
}
