package uni.da.node;


import lombok.*;
import uni.da.common.Addr;
import uni.da.common.Pipe;
import uni.da.remote.RaftRpcService;

import java.io.IOException;
import java.io.Serializable;
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
public class ConsensusState implements Serializable {

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






    /** Persistent state on all servers */

    // latest term server has seen (initialized to 0 on first boot, increases monotonically)
    public AtomicInteger currTerm = new AtomicInteger(0);

    // candidateId that received vote in current term (or null if none)
    public volatile Integer votedFor = null;

    // log entries; each entry contains command for state machine, and term when entry was received by leader (first index is 1)
    private volatile LogModule logModule;


    /** Volatile state on all servers */

    /**
     * 1. commitIndex: index of highest log entry known to be
     *  committed (initialized to 0, increases
     *  monotonically)
     *
     *  Obtain from Logmodule
     */

    /**
     * 2. lastApplied
     * index of highest log entry applied to state
     * machine (initialized to 0, increases
     * monotonically)
     */

    /**
     * for each server, index of the next log entry
     * to send to that server (initialized to leader
     * last log index + 1)
     */
    private Map<Integer, Integer> nextIndex = new HashMap<>();


    /**
     * for each server, index of the highest log entry
     * known to be replicated on server
     * (initialized to 0, increases monotonically)
     */
    private Map<Integer, Integer> matchIndex = new HashMap<>();



    // 集群leader id
    public AtomicInteger leaderId = new AtomicInteger(-1);


    // 节点角色
    private volatile Character character = Character.Follower;

    // 每个任期投票历史，并发稳定读写不可抢占
    private volatile ConcurrentHashMap<Integer, Integer> voteHistory = new ConcurrentHashMap<>();



    public ConsensusState(int id, String name, Addr addr, int timeout, Map<Integer, Addr> clusterAddr) throws IOException {
        this.id = id;
        this.name = name;
        this.addr = addr;
        this.timeout = timeout;
        this.pipe = new Pipe("hearBeat");
        this.clusterAddr = clusterAddr;
        restore();
    }



    /**
     * TODO Restore from persistence state
     */
    private void restore() {
        clusterAddr.forEach((k,v) -> {
            nextIndex.put(k, this.getLogModule().getPrevLogIndex() + 1);
            matchIndex.put(k, 0);
        });
    }


}
