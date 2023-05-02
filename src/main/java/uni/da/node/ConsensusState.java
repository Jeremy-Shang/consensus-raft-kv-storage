package uni.da.node;


import lombok.*;
import uni.da.common.Addr;
import uni.da.common.Pipe;
import uni.da.entity.Log.LogEntry;
import uni.da.remote.RaftRpcService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/*
    节点信息
 */
@Data
@ToString
public class ConsensusState {

    // 确保节点参数对象唯一
    private static ConsensusState consensusState;

    /** 节点固定配置参数 */
    private final int id;

    // 名字，用来debug
    private final String name;

    // 端口 + 地址
    private final Addr addr;

    // 超时时长，毫秒
    private final int timeout;

    // 集群中其他所有的节点的配置
    private Map<Integer, Addr> clusterAddr;

    // 心跳监听的阻塞式管道
    private Pipe pipe;
    
    // 远程服务
    private Map<Integer, RaftRpcService> remoteServiceMap;

    // 节点公共线程池
    private ExecutorService nodeExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);


    /** 节点动态参数 */
    // 当前任期
    public AtomicInteger term = new AtomicInteger();

    // 日志模块 （包含日志体）
    private volatile LogModule logModule;

    // 节点角色
    private volatile Character character = Character.Follower;

    // 每个任期投票历史，并发稳定读写不可抢占
    private volatile ConcurrentHashMap<Integer, Integer> voteHistory = new ConcurrentHashMap<>();

    // 无并发问题
    // 作为leader，下一个该给节点发送什么index日志
    private Map<Integer, Integer> nextIndex = new HashMap<>();


    // 作为leader，对面节点目前复制到的最高日志信息
    private Map<Integer, Integer> matchIndex = new HashMap<>();



    public ConsensusState(int id, String name, Addr addr, int timeout) throws IOException {
        this.id = id;
        this.name = name;
        this.addr = addr;
        this.timeout = timeout;

        this.pipe = new Pipe("hearBeat");

        // 初始状态下
        clusterAddr.forEach((k,v) -> {
            nextIndex.put(k, 1);
            matchIndex.put(k, 0);
        });

    }



    private ConsensusState(int id, String name, Addr addr, int[] timeoutRange) throws IOException {
        this.id = id;
        this.name = name;
        this.addr = addr;
        this.timeout = new Random().nextInt(timeoutRange[1] - timeoutRange[0] + 1) + timeoutRange[0];

        nextIndex.replaceAll((k, v) -> 1);
        matchIndex.replaceAll((k, v) -> 0);

        this.pipe = new Pipe("hearBeat");
    }

    public static ConsensusState getInstance(int id, String name, Addr addr, int[] timeoutRange) throws IOException {
        if (consensusState == null) {
            consensusState = new ConsensusState(id, name, addr, timeoutRange);
        }
        return consensusState;
    }


}
