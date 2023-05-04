package uni.da.remote.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uni.da.entity.*;
import uni.da.entity.Log.LogBody;
import uni.da.entity.Log.LogEntry;
import uni.da.node.Character;
import uni.da.node.ConsensusState;
import uni.da.remote.RaftRpcService;
import uni.da.statemachine.fsm.component.EventType;
import uni.da.task.BroadcastTask;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Data
public class RaftRpcServiceImpl extends UnicastRemoteObject implements RaftRpcService {

    private ConsensusState consensusState;

    public RaftRpcServiceImpl(ConsensusState consensusState) throws RemoteException {
        super();
        this.consensusState = consensusState;

    }

    /**
     *  1. Reply false if term < currentTerm
     *  2. If votedFor is null or candidateId, and candidate’s log is at
     *      least as up-to-date as receiver’s log, grant vote
     * @param request
     * @return
     */
    @Override
    public RequestVoteResponse requestVote(RequestVoteRequest request) {

        resetTimer();
        termCheck(request.getTerm());




        RequestVoteResponse reject = RequestVoteResponse.builder()
                .term(consensusState.getCurrTerm().get())
                .voteGranted(false)
                .build();

        if (request.getTerm() < consensusState.getCurrTerm().get()) {
            log.info("[VOTE REJECT (1)] to node{}; currTerm {}, candidate term {}. ", request.getCandidateId(), consensusState.getCurrTerm(), request.getTerm());
            return RequestVoteResponse.builder()
                    .term(consensusState.getCurrTerm().get())
                    .voteGranted(false)
                    .build();
        }

        if ((consensusState.votedFor == null || consensusState.votedFor == request.getCandidateId())
        &&(request.getLastLogTerm() > consensusState.getLogModule().getLastLogTerm() || (request.getLastLogTerm() == consensusState.getCurrTerm().get() && request.getLastLogIndex() > consensusState.getLogModule().getLastLogIndex()))) {
            log.info("[VOTE GRANTED to node{}. ", request.getCandidateId());
            return RequestVoteResponse.builder()
                    .term(consensusState.getCurrTerm().get())
                    .voteGranted(true)
                    .build();
        }

        log.info("[VOTE REJECT (2)] to node{}; ", request.getCandidateId());

        return RequestVoteResponse.builder()
                .term(consensusState.getCurrTerm().get())
                .voteGranted(false)
                .build();
    }


    /**
     * 1. Reply false if term < currentTerm (§5.1)
     * 2. Reply false if log doesn’t contain an entry at prevLogIndex
     *      whose term matches prevLogTerm (§5.3)
     * 3. If an existing entry conflicts with a new one (same index
     *      but different terms), delete the existing entry and all that
     *      follow it (§5.3)
     * 4. Append any new entries not already in the log
     * 5. If leaderCommit > commitIndex, set commitIndex =
     *      min(leaderCommit, index of last new entry)
     * @param request
     * @return
     */
    @Override
    public AppendEntryResponse appendEntry(AppendEntryRequest request) {
        /**
         * 1.reset listening heartbeat thread
         * 2.Term set to leader's term?
         * 3.character change?
         * 4.set current leader's id?
         */
        resetTimer();
        termCheck(request.getTerm());


        if (request.getTerm() < consensusState.getCurrTerm().get()) {
            log.info("[REJECT APPEND ENTRY (1)] from node{}, currTerm {}, senderTerm {}", consensusState.getCurrTerm().get(), request.getTerm());
            return AppendEntryResponse.builder()
                    .term(consensusState.getCurrTerm().get())
                    .success(false)
                    .build();
        }

        if (consensusState.getLogModule()
                .getLogEntry(request.getPrevLogIndex(), request.getPrevLogIndex()) == null) {
            log.info("[REJECT APPEND ENTRY (2)] from node{}. ", request.getLeaderId());
            return AppendEntryResponse.builder()
                    .term(consensusState.getCurrTerm().get())
                    .success(false)
                    .build();
        }

        LogEntry newLogEntry = request.getLogEntry();

        if (consensusState.getLogModule().getEntryByIndex(newLogEntry.getLogIndex()).getTerm() != newLogEntry.getTerm()) {
            // TODO  delete the existing entry and all that follow it ?
        }

        consensusState.getLogModule().append(request.getLogEntry());


        if (request.getLeaderCommit() > consensusState.getCommitIndex().get()) {
            int newCommitIndex = Math.min(request.getLeaderCommit(),
                    consensusState.getLogModule().getLastLogIndex());

            consensusState.setCommitIndex(newCommitIndex);
        }

        return AppendEntryResponse.builder()
                .term(consensusState.getCurrTerm().get())
                .success(true)
                .build();
//        // 1. 任期号跟Leader同步
//        consensusState.getCurrTerm().set(request.getTerm());
//        // 2. 状态变更为Follower
//        consensusState.setCharacter(Character.Follower);
//        // 3. TODO 设置当前leaderID
//        consensusState.getLeaderId().getAndSet(request.getLeaderId());
    }

    @Override
    public ClientResponse handleClient(ClientRequest request) throws ExecutionException, InterruptedException {

        log.info(request.toString());

        // 1. TODO get request
        if (request.getTYPE() == 1) {
            return null;
        }


        int key = request.getKey();
        String val = request.getVal();

        // 2. TODO 重定向到leader
        if (consensusState.getCharacter() != Character.Leader) {
            return null;
        }

        // 2. 当前leader中直接插入指令
        LogEntry logEntry = LogEntry.builder()
                .term(consensusState.getCurrTerm().get())
                .logIndex(consensusState.getLogModule().getLastLogIndex() + 1)
                .body(new LogBody(key ,val))
                .build();

        consensusState.getLogModule().append(logEntry);

        // 3. 发起消息广播
        Future<EventType> future =  consensusState.getNodeExecutorService().submit(new BroadcastTask(consensusState));

        try {
            future.get();
        } catch (Exception e) {
            log.error("任务失败");
        }


        // 4. 客户端回显数据
        Map<Integer, List<LogEntry>> clientEcho = new HashMap<>();

        for(Integer k: consensusState.getRemoteServiceMap().keySet()) {

            RaftRpcService s = consensusState.getRemoteServiceMap().get(k);

            CopyOnWriteArrayList<LogEntry> logEntries = null;
            try {
                logEntries = s.gatherClusterLogEntries();
            } catch (Exception e) {
                log.error("获取节点日志失败");
            }
            clientEcho.put(k, new ArrayList<>(logEntries));
        }


        // 4. TODO 客户端需要回显
        return ClientResponse.success(clientEcho);
    }


    /**
     * 客户端回显，获取所有节点log状态
     * @return
     */
    @Override
    public CopyOnWriteArrayList<LogEntry> gatherClusterLogEntries() {
        return consensusState.getLogModule().getLogEntries();
    }


    /**
     * If RPC response contains term T > currentTerm:
     * set currentTerm = T, convert to follower (§5.1)
     */
    private void termCheck(int term) {
        if (term > consensusState.getCurrTerm().get()) {
            consensusState.getCurrTerm().set(term);

            consensusState.setCharacter(Character.Follower);
        }
    }


    /**
     * Reset timer for listening heartbeat task
     */
    private void resetTimer() {
        try {
            this.consensusState.getPipe().getOutputStream().write(1);
        } catch (IOException e) {
            log.error("Maintain heartBeat fail: node{}", consensusState.getId());
        }
    }



    @Override
    public void sayHi() {
        log.info("Hello World !!!!!!!!!!!!!!!!");
    }

}
