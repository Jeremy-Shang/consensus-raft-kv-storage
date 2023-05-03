package uni.da.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {


    public String sayHello() throws RemoteException;


}
