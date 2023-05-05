package uni.da.node;


import lombok.*;
import uni.da.common.Addr;
import uni.da.common.Pipe;
import uni.da.remote.RaftRpcService;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Consensus state contains shared information of each node
 * Object is singleton and can be shared by thread
 * inside node or other nodes
 */
@Data
@ToString
public class ConsensusState implements Serializable {
    private static ConsensusState consensusState;

    /** Node common information */
    private final int id;

    private final String name;

    private final Addr addr;

    // TODO random timeout
    private final int timeout;

    // Cluster configuration. All nodes share
    private Map<Integer, Addr> clusterAddr;

    private final Integer clusterSize;

    // Using pipe's blocking read as timer
    private Pipe timer;
    
    // Contain rpc communication method to each node
    private Map<Integer, RaftRpcService> remoteServiceMap;

    // All threads are running in a node thread pool
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
     * 1. commitIndex: index of the highest log entry known to be
     *  committed (initialized to 0, increases
     *  monotonically)
     *
     *  Obtain from Logmodule
     */

    private AtomicInteger commitIndex;

    /**
     * 2. lastApplied
     * index of the highest log entry applied to state
     * machine (initialized to 0, increases
     * monotonically)
     */
    private AtomicInteger lastApplied;

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
        this.timer = new Pipe("hearBeat");
        this.clusterAddr = clusterAddr;
        this.clusterSize = clusterAddr.size();
        restore();
    }

    /**
     * If commitIndex > lastApplied: increment lastApplied, apply
     * log[lastApplied] to state machine (§5.3)
     * @param newCommitIndex
     */
    public void setCommitAndApply(int newCommitIndex) {

        this.commitIndex.set(newCommitIndex);

        if (this.commitIndex.get() > this.lastApplied.get()) {

            this.lastApplied.incrementAndGet();

            this.logModule.apply(this.lastApplied.get());
        }

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
