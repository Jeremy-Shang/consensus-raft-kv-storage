package uni.da.common;


import lombok.Data;
import uni.da.status.Status;

import java.util.ArrayList;
import java.util.List;

@Data
public class NodeConfig {

    // 可以直接配置节点的角色
    private Status status;

    // 自己的地址
    private String addr;

    // 集群中其他所有的节点地址
    private List<String> clusterAddr;

}
