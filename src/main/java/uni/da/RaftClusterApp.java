package uni.da;

import lombok.extern.slf4j.Slf4j;
import uni.da.common.Addr;
import uni.da.node.ConsensusState;
import uni.da.node.Node;
import uni.da.node.impl.NodeModuleImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RaftClusterApp {

    public static void main(String[] args) throws IOException, InterruptedException {
        // 6666, 6667, 6668, 6669, 6670

        int id = Integer.parseInt(System.getProperty("id"));

        int port = Integer.parseInt(System.getProperty("port"));

        int timeout = Integer.parseInt(System.getProperty("timeout"));

        String ip = "127.0.0.1";

        Map<Integer, Addr> clusterAddr = new HashMap<>();
        clusterAddr.put(1, new Addr(ip, 6666));
        clusterAddr.put(2, new Addr(ip, 6667));
        clusterAddr.put(3, new Addr(ip, 6668));
        clusterAddr.put(4, new Addr(ip, 6669));
        clusterAddr.put(5, new Addr(ip, 6670));


        ConsensusState consensusState = ConsensusState.getInstance(id, "node"+id, new Addr(ip, port), timeout, clusterAddr);

        Node node = NodeModuleImpl.getInstance(consensusState);

        node.start();
    }

}