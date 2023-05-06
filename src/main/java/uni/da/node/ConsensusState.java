package uni.da.node;


import lombok.*;
import uni.da.common.Addr;
import uni.da.common.Timer;
import uni.da.entity.Log.LogBody;
import uni.da.node.impl.LogModuleImpl;
import uni.da.node.impl.StateMachineImpl;
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
@Getter
@Setter
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
    private Timer timer;
    
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

    // TODO: check statemachine usge
    private volatile StateMachineModule stateMachineModule;



    /** Volatile state on all servers */

    /**
     * 1. commitIndex: index of the highest log entry known to be
     *  committed (initialized to 0, increases
     *  monotonically)
     *
     *  Obtain from Logmodule
     */

    private AtomicInteger commitIndex = new AtomicInteger(0);

    /**
     * 2. lastApplied
     * index of the highest log entry applied to state
     * machine (initialized to 0, increases
     * monotonically)
     */
    private AtomicInteger lastApplied = new AtomicInteger(0);

    /**
     * for each server, index of the next log entry
     * to send to that server (initialized to leader
     * last log index + 1)
     */
    private Map<Integer, Integer> nextIndex = new ConcurrentHashMap<>();

    /**
     * for each server, index of the highest log entry
     * known to be replicated on server
     * (initialized to 0, increases monotonically)
     */
    private Map<Integer, Integer> matchIndex = new ConcurrentHashMap<>();




    // 集群leader id
    public AtomicInteger leaderId = new AtomicInteger(-1);

    // 节点角色
    private volatile Character character = Character.Follower;

    // 每个任期投票历史，并发稳定读写不可抢占
    private volatile ConcurrentHashMap<Integer, Integer> voteHistory = new ConcurrentHashMap<>();

    private ConsensusState(int id, String name, Addr addr, int timeout, Map<Integer, Addr> clusterAddr) throws IOException {
        this.id = id;
        this.name = name;
        this.addr = addr;
        this.timeout = timeout;
        this.timer = Timer.getInstance();

        this.clusterAddr = clusterAddr;
        this.clusterSize = clusterAddr.size();

        this.logModule = new LogModuleImpl(String.valueOf(id));
        this.stateMachineModule = new StateMachineImpl(String.valueOf(id));

    }

    public static synchronized ConsensusState getInstance(int id, String name, Addr addr, int timeout, Map<Integer, Addr> clusterAddr) throws IOException {
        if (ConsensusState.consensusState == null)  {
            ConsensusState.consensusState = new ConsensusState(id ,name, addr, timeout, clusterAddr);
        }

        return ConsensusState.consensusState;
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

            LogBody logBody = this.logModule.getEntryByIndex(this.lastApplied.get()).getBody();

            this.stateMachineModule.commit(logBody);
        }
    }

    public void setCharacter(Character ch) {
        this.character = ch;
        if (this.character == Character.Leader) {
            this.clusterAddr.entrySet().forEach(
                    entry ->  {
                        if (entry.getKey() != consensusState.getId()) {
                            this.matchIndex.put(entry.getKey(), 0);
                            this.nextIndex.put(entry.getKey(), consensusState.getLogModule().getLastLogIndex() + 1);
                        }
                    }
            );
        }
    }
}
