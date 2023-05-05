package uni.da.remote.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uni.da.entity.*;
import uni.da.entity.Log.LogBody;
import uni.da.entity.Log.LogEntry;
import uni.da.node.Character;
import uni.da.node.ConsensusState;
import uni.da.remote.RaftRpcService;
import uni.da.statetransfer.fsm.component.EventType;
import uni.da.task.BroadcastTask;
import uni.da.util.LogUtil;

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


        termCheck(request.getTerm());

        if (request.getTerm() < consensusState.getCurrTerm().get()) {
            log.info("[VOTE REJECT (1)] to node{}; currTerm {}, candidate term {}. ", request.getCandidateId(), consensusState.getCurrTerm(), request.getTerm());
            return RequestVoteResponse.builder()
                    .term(consensusState.getCurrTerm().get())
                    .voteGranted(false)
                    .build();
        }

        if ((consensusState.votedFor == null || consensusState.votedFor == request.getCandidateId())
        && (request.getLastLogTerm() > consensusState.getLogModule().getLastLogTerm() ||
                (request.getLastLogTerm() == consensusState.getLogModule().getLastLogTerm() && request.getLastLogIndex() >= consensusState.getLogModule().getLastLogIndex()))) {


            log.info("[VOTE GRANTED to node{}. ", request.getCandidateId());
            resetTimer();
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

        termCheck(request.getTerm());

        if (request.getTerm() < consensusState.getCurrTerm().get()) {
            log.info("[REJECT APPEND ENTRY (1)] from node{}, currTerm {}, senderTerm {}", consensusState.getCurrTerm().get(), request.getTerm());
            return AppendEntryResponse.builder()
                    .term(consensusState.getCurrTerm().get())
                    .success(false)
                    .build();
        }

        // leader's rpc is not outdated. reset timer.
        resetTimer();

        if (consensusState.getLogModule()
                .getLogEntry(request.getPrevLogIndex(), request.getPreLogTerm()) == null) {
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

            consensusState.setCommitAndApply(newCommitIndex);
        }

        return AppendEntryResponse.builder()
                .term(consensusState.getCurrTerm().get())
                .success(true)
                .build();

    }


    /**
     * Leaders:
     * 1. If command received from client: append entry to local log,
     *      respond after entry applied to state machine (§5.3)
     *
     *
     * @param request
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public ClientResponse handleClient(ClientRequest request) throws ExecutionException, InterruptedException {

        LogUtil.printBoxedMessage("Receive client request: " + request);

        // append to local log
        consensusState.getLogModule().append(LogEntry.builder()
                .term(consensusState.getCurrTerm().get())
                .logIndex(consensusState.getLogModule().getLastLogIndex() + 1)
                .body(new LogBody(request.getKey() ,request.getVal()))
                .build());


        // send to followers
        Future<EventType> future =  consensusState.getNodeExecutorService().submit(
                new BroadcastTask(consensusState));



        // TODO
        try {
            future.get();
        } catch (Exception e) {
            log.error("fail");
        }

        Map<Integer, List<LogEntry>> clientEcho = new HashMap<>();

        for(Integer k: consensusState.getRemoteServiceMap().keySet()) {

            RaftRpcService s = consensusState.getRemoteServiceMap().get(k);

            CopyOnWriteArrayList<LogEntry> logEntries = null;
            try {
                logEntries = s.gatherClusterLogEntries();
            } catch (Exception e) {
                log.error("fail");
            }
            clientEcho.put(k, new ArrayList<>(logEntries));
        }

        return ClientResponse.success(clientEcho);
    }



    /**
     * Client echo
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

            // TODO: need reset voteFor for new term?
            consensusState.votedFor = null;
        }
    }







    /**
     * Reset timer for listening heartbeat task
     */
    private void resetTimer() {
        try {
            this.consensusState.getTimer().getOutputStream().write(1);
        } catch (IOException e) {
            log.error("Maintain heartBeat fail: node{}", consensusState.getId());
        }
    }


    @Override
    public void sayHi() {
        log.info("Hello World !!!!!!!!!!!!!!!!");
    }

}
