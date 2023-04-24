package uni.da.common;


import lombok.*;
import uni.da.node.LogModule;
import uni.da.remote.RaftRpcService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/*
    节点信息
 */
@Data
@ToString
@Builder
public class NodeParam {

    // 确保节点参数对象唯一
    private static NodeParam nodeParam;

    /** 节点固定配置参数 */
    private final int id;

    // 名字，用来debug
    private final String name;

    // 端口 + 地址
    private final Addr addr;

    // 超时时长区间
    private final int timeout;

    // 集群中其他所有的节点的配置
    private Map<Integer, Addr> clusterAddr;

    // 心跳监听的阻塞式管道
    private Pipe pipe;
    
    // 远程服务
    private Map<Integer, RaftRpcService> remoteServiceMap;


    /** 节点动态参数 */
    // 当前任期
    private AtomicInteger term;

    // 日志模块 （包含日志体）
    private volatile LogModule logModule;


    private NodeParam(int id, String name, Addr addr, int[] timeoutRange) throws IOException {
        this.id = id;
        this.name = name;
        this.addr = addr;
        this.timeout = new Random().nextInt(timeoutRange[1] - timeoutRange[0] + 1) + timeoutRange[0];

        this.pipe = new Pipe("hearBeat");
    }

    public static NodeParam getInstance(int id, String name, Addr addr, int[] timeoutRange) throws IOException {
        if (nodeParam == null) {
            nodeParam = new NodeParam(id, name, addr, timeoutRange);
        }
        return nodeParam;
    }
}
