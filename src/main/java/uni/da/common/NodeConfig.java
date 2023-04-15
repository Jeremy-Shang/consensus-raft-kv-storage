package uni.da.common;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uni.da.node.Node;
import uni.da.status.Status;

import java.util.ArrayList;
import java.util.List;

/*
    每个节点的配置信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NodeConfig {

    // 自己的地址
    private String addr;

    // 超时时长
    private int timeout;

    // 名字，用来debug
    private String name;

    // 集群中其他所有的节点地址
    private List<String> clusterAddr;
}
