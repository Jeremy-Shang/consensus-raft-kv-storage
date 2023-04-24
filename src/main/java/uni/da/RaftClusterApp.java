package uni.da;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;
import uni.da.common.NodeConfig;
import uni.da.node.Node;
import uni.da.node.impl.NodeImpl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class RaftClusterApp {

    public static Node getNode() throws IOException {
        Node node;

        List<NodeConfig> nodeConfigList = new ArrayList<>();

        // 解析配置文件
        String path = RaftClusterApp.class.getResource("/raft.yaml").getPath();
        Yaml yaml = new Yaml();
        Map<String, List<Map<String, Object>>> yamlConfig = yaml.load(new FileInputStream(path));

        // 取得集群中所有节点配置
        List<String> address = yamlConfig.get("nodes")
                .stream()
                .map(m -> (String) m.get("address"))
                .collect(Collectors.toList());

        yamlConfig.get("nodes")
                .stream()
                .forEach(m ->  {
                    String ip = (String) m.get("ip");
                    int port = (Integer) m.get("port");
                    String name = (String) m.get("name");

                    NodeConfig config = new NodeConfig(name, ip, port);
                    nodeConfigList.add(config);
                });

        // 获取当前节点的配置信息
        int port = Integer.parseInt(System.getProperty("port"));
        String ip = System.getProperty("ip");
        String name = (String) yamlConfig.get("nodes")
                                .stream()
                                .filter(e -> e.get("ip").equals(ip) && (Integer)e.get("port") == port)
                                .findAny()
                                .get()
                                .get("name");

        NodeConfig config = new NodeConfig(name, ip, port);
        config.setClusterConfig(nodeConfigList);
        node = NodeImpl.getInstance(config);

        log.info("raft集群配置信息: " + nodeConfigList.toString());
        log.info("当前节点配置信息: " + config.toString());
        return node;
    }


    public static void main(String[] args) throws IOException, InterruptedException {

        RaftClusterApp.getNode().start();

    }

}