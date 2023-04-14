package uni.da.common;


import lombok.Data;
import uni.da.status.Status;

import java.util.ArrayList;
import java.util.List;

@Data
public class NodeConfig {

    // 自己的地址
    private String addr;

    // 集群中其他所有的节点地址
    private List<String> clusterAddr;

    // 超时时长
    private int timeout;

    // 名字，用来debug
    private String name;
}
