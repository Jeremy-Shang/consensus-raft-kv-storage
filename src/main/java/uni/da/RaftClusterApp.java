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

//    public static Node getNode() throws IOException {
//        Node node;

//        List<NodeParam> nodeConfigList = new ArrayList<>();

//        // 解析配置文件
//        String path = RaftClusterApp.class.getResource("/raft.yaml").getPath();
//        Yaml yaml = new Yaml();
//        Map<String, List<Map<String, Object>>> yamlConfig = yaml.load(new FileInputStream(path));
//
//        // 取得集群中所有节点配置
//        List<String> address = yamlConfig.get("nodes")
//                .stream()
//                .map(m -> (String) m.get("address"))
//                .collect(Collectors.toList());
//
//        yamlConfig.get("nodes")
//                .stream()
//                .forEach(m ->  {
//                    String ip = (String) m.get("ip");
//                    int port = (Integer) m.get("port");
//                    String name = (String) m.get("name");
//
//                    NodeParam config = new NodeParam(name, ip, port);
//                    nodeConfigList.add(config);
//                });
//
//        // 获取当前节点的配置信息
//        int port = Integer.parseInt(System.getProperty("port"));
//        String ip = System.getProperty("ip");
//        String name = (String) yamlConfig.get("nodes")
//                                .stream()
//                                .filter(e -> e.get("ip").equals(ip) && (Integer)e.get("port") == port)
//                                .findAny()
//                                .get()
//                                .get("name");
//
//        NodeParam config = new NodeParam(name, ip, port);
//        config.setClusterConfig(nodeConfigList);
//        node = NodeImpl.getInstance(config);
//
//        log.info("raft集群配置信息: " + nodeConfigList.toString());
//        log.info("当前节点配置信息: " + config.toString());
//        return node;
//    }


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
//        clusterAddr.put(4, new Addr(ip, 6669));
//        clusterAddr.put(5, new Addr(ip, 6670));


        ConsensusState consensusState = ConsensusState.getInstance(id, "node"+id, new Addr(ip, port), timeout, clusterAddr);

        Node node = NodeModuleImpl.getInstance(consensusState);

        node.start();
    }

}