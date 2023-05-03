package uni.da.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) {
        try {
            String host = "127.0.0.1";
            int port = 6666;
            String name = "RemoteObject";

            Registry registry = LocateRegistry.getRegistry(host, port);
            RemoteInterface remoteObject = (RemoteInterface) registry.lookup(name);

            System.out.println(remoteObject.sayHello());
        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}