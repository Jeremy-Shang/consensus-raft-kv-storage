package uni.da.rmi;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;

public class RemoteObject extends UnicastRemoteObject implements RemoteInterface {
    public RemoteObject() throws RemoteException {
        super();
    }

    @Override
    public String sayHello() {
        return "Hello, world!";
    }

    public static void main(String[] args) {
        try {
            RemoteObject remoteObject = new RemoteObject();
            Registry registry = LocateRegistry.createRegistry(6666);
            registry.rebind("RemoteObject", remoteObject);
            System.out.println("RemoteObject is ready.");
        } catch (Exception e) {
            System.err.println("RemoteObject exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
