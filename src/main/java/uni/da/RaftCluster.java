package uni.da;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;
import uni.da.common.NodeConfig;
import uni.da.node.Node;
import uni.da.node.impl.NodeImpl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class RaftCluster {
    public static RaftCluster raftCluster = null;

    // 集群配置
    private List<NodeConfig> nodeConfigList = new ArrayList<>();

    private RaftCluster() throws FileNotFoundException {
        // 解析配置文件
        String path = RaftCluster.class.getResource("/raft.yaml").getPath();
        Yaml yaml = new Yaml();
        Map<String, List<Map<String, Object>>> yamlConfig = yaml.load(new FileInputStream(path));

        // 取得所有节点地址
        List<String> address = yamlConfig.get("nodes")
                .stream()
                .map(m -> (String) m.get("address"))
                .collect(Collectors.toList());

//        // 构造节点配置
//        yamlConfig.get("nodes")
//                .stream()
//                .forEach(m ->  {
//                    String addr = (String) m.get("address"), name = (String) m.get("name");
//                    int timeout = (int) m.get("timeout");
//                    NodeConfig config = new NodeConfig(addr, timeout, name, address);
//                    nodeConfigList.add(config);
//                });

        log.info("集群配置：" + nodeConfigList.toString());
    }

    public static RaftCluster getInstance() throws FileNotFoundException {
        if (raftCluster == null) {
            raftCluster = new RaftCluster();
        }
        return raftCluster;
    }

    // TODO 生成多进程场景下的进程启动命令
    public List<String> getCommand() {
        return null;
    }

    public List<NodeConfig> getConfig() {
        return nodeConfigList;
    }

    // 单进程多线程方式启动
    public static void runMultiThreadRaftCluster() throws FileNotFoundException {
        RaftCluster raftCluster = RaftCluster.getInstance();
        // 启动节点
        raftCluster.getConfig()
                .forEach(config -> {
                    Node node = new NodeImpl(config);
                    node.start();
                });
    }

    // TODO 多进程方式一键启动
    public static void runMultiProcessRaftCluster() {
        Runtime.getRuntime();
    }

    public static void main(String[] args) throws FileNotFoundException {

        RaftCluster.runMultiThreadRaftCluster();

    }

}