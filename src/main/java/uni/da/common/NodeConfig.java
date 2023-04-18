package uni.da.common;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uni.da.node.Node;
import uni.da.status.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/*
    每个节点的配置信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NodeConfig {

    // 随机生成，当作分布式全局唯一ID
    private String uuid;

    // 自己的地址
    private String ip;

    // 端口
    private int port;

    // 超时时长
    private int timeout;

    // 名字，用来debug
    private String name;

    // 集群中其他所有的节点的配置
    private List<NodeConfig> clusterConfig;

    public NodeConfig(String name, String ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;

        uuid = String.valueOf(UUID.randomUUID());
        // 150～500ms的随机超时时间
        int min = 500, max = 1000;
        Random random = new Random();
        timeout = random.nextInt(max - min + 1) + min;
    }
}
