package uni.da.remote.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uni.da.entity.*;
import uni.da.entity.Log.LogBody;
import uni.da.entity.Log.LogEntry;
import uni.da.node.Character;
import uni.da.node.ConsensusState;
import uni.da.remote.RaftRpcService;
import uni.da.task.BroadcastTask;
import uni.da.util.LogType;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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


        termCheck(request.getTerm(), request.getCandidateId(), "requestVote()");

        if (request.getTerm() < consensusState.getCurrTerm().get()) {
            log.info("[{} (1)] to node{}; currTerm {}, candidate term {}. ", LogType.VOTE_REJECT, request.getCandidateId(), consensusState.getCurrTerm(), request.getTerm());
            return RequestVoteResponse.builder()
                    .term(consensusState.getCurrTerm().get())
                    .voteGranted(false)
                    .build();
        }

        if ((consensusState.votedFor == null || consensusState.votedFor == request.getCandidateId())
        && (request.getLastLogTerm() > consensusState.getLogModule().getLastLogTerm() ||
                (request.getLastLogTerm() == consensusState.getLogModule().getLastLogTerm() && request.getLastLogIndex() >= consensusState.getLogModule().getLastLogIndex()))) {


            log.info("[{}] to node{}. ", LogType.VOTE_SUCCESS, request.getCandidateId());
            resetTimer(request.getCandidateId(), "requestVote()");
            return RequestVoteResponse.builder()
                    .term(consensusState.getCurrTerm().get())
                    .voteGranted(true)
                    .build();
        }

        log.info("[{} (2)] to node{}; ", LogType.VOTE_REJECT, request.getCandidateId());

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

        termCheck(request.getTerm(), request.getLeaderId(), "appendEntry()");

        if (request.getTerm() < consensusState.getCurrTerm().get()) {
            log.info("[REJECT APPEND ENTRY (1)] from node{}, currTerm {}, senderTerm {}", request.getLeaderId(), consensusState.getCurrTerm(), request.getTerm());
            return AppendEntryResponse.builder()
                    .term(consensusState.getCurrTerm().get())
                    .success(false)
                    .build();
        }

        // leader's rpc is not outdated. reset timer.
        resetTimer(request.getLeaderId(), "appendEntry");
        consensusState.getLeaderId().set(request.getLeaderId());

        if (consensusState.getLogModule()
                .getLogEntry(request.getPrevLogIndex(), request.getPreLogTerm()) == null) {
            log.info("[REJECT APPEND ENTRY (2)] from node{}. logEntry(index={}, term={}) do not exist.", request.getLeaderId(), request.getPrevLogIndex(), request.getPreLogTerm());
            return AppendEntryResponse.builder()
                    .term(consensusState.getCurrTerm().get())
                    .success(false)
                    .build();
        }

        LogEntry newLogEntry = request.getLogEntry();

        if (newLogEntry != null) {
            int logIndex = newLogEntry.getLogIndex();

            LogEntry oldLogEntry = consensusState.getLogModule().getLogEntry(logIndex);

            if (oldLogEntry != null && newLogEntry.getTerm() == oldLogEntry.getTerm()) {
                // TODO  delete the existing entry and all that follow it
                List<LogEntry> removeIndexes = consensusState.getLogModule().getLogEntries()
                        .stream().filter(e -> e.getLogIndex() >= logIndex)
                        .collect(Collectors.toList());

                removeIndexes.forEach(e -> consensusState.getLogModule().removeEntry(e.getLogIndex()));
            }

        }

        if (request.getLogEntry() != null && !consensusState.getLogModule().contains(request.getLogEntry())) {
            consensusState.getLogModule().append(request.getLogEntry());
        }

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
    public ClientResponse handleClient(ClientRequest request) throws ExecutionException, InterruptedException, RemoteException {

        log.info("[{}: client request: {} ]", LogType.RECEIVE, request);

        int leaderId = consensusState.getLeaderId().get();

        if (leaderId != consensusState.getId()) {
            log.info("[REDIRECT] redirect client's message to leader. {} -> {}", consensusState.getId(), leaderId);

            return consensusState.getRemoteServiceMap().get(leaderId).handleClient(request);
        }



        ClientResponse<List<Map<Integer, List<LogEntry>>>> clientResponse = new ClientResponse<>();
        clientResponse.setData(new ArrayList<>());

        // append to local log
        consensusState.getLogModule().append(LogEntry.builder()
                .term(consensusState.getCurrTerm().get())
                .logIndex(consensusState.getLogModule().getLastLogIndex() + 1)
                .body(new LogBody(request.getKey() ,request.getVal()))
                .build());

        // demo: showing cluster logs before synchronized
        clientResponse.getData().add(new ConcurrentHashMap<>(consensusState.getPeersLogs()));
        log.info("before {} ", consensusState.getPeersLogs());

        // send to followers
        consensusState.getNodeExecutorService().submit(
                new BroadcastTask(consensusState));

        // wait for followers synchronized
        Thread.sleep(2000);

        // gather followers' logs
        Map<Integer, List<LogEntry>> peers = new HashMap<>();
        List<Integer> followers = new ArrayList<>(consensusState.getRemoteServiceMap().keySet());
        followers.remove(Integer.valueOf(consensusState.getId()));

        for(Integer k: followers) {

            RaftRpcService s = consensusState.getRemoteServiceMap().get(k);

            CopyOnWriteArrayList<LogEntry> logEntries = null;
            try {
                logEntries = s.gatherClusterLogEntries();
                consensusState.getPeersLogs().put(k, new ArrayList<>(logEntries));
            } catch (Exception e) {
                log.error("fail");
            }

        }

        clientResponse.getData().add(new ConcurrentHashMap<>(consensusState.getPeersLogs()));
        log.info("after {} ", consensusState.getPeersLogs());


        return clientResponse;
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
    private void termCheck(int term, int id, String method) {
        if (term > consensusState.getCurrTerm().get()) {

            log.info("[{}] {} {} receive {} from {} contains greater terms. currTerm: {}, requestTerm: {}. Switch to Follower"
                    , LogType.CHARACTER_CHANGE
                    , consensusState.getCharacter()
                    , consensusState.getName()
                    , method
                    , id
                    ,consensusState.getCurrTerm()
                    ,term);

            consensusState.getCurrTerm().set(term);

            consensusState.setCharacter(Character.Follower);

            // TODO: need reset voteFor for new term?
            consensusState.votedFor = null;
        }
    }


    /**
     * Reset timer for listening heartbeat task
     */
    private void resetTimer(int id, String method) {

        synchronized (this.consensusState.getTimer().TimerLock) {
            try {
                log.debug("[TIMER RESET] by node{} in {}. {} {}: {}.", id, method, consensusState.getCharacter(), consensusState.getName(), consensusState.getTimeout());
                this.consensusState.getTimer().getOutputStream().write(1);
            } catch (IOException e) {
                log.error("Leader maintain heartBeat fail: node{}", consensusState.getId());
                e.printStackTrace();
            }
        }

    }


    @Override
    public void sayHi() {
        log.info("\u001B[3mSay Hi~\u001B[0m");
    }


    public static void main(String[] args) {
        ConcurrentHashMap<Integer, Integer> m = new ConcurrentHashMap<>();

        m.put(1, 1);
        m.put(2, 1);
        m.put(3, 1);
        m.put(4, 1);
        m.put(5, 1);


        List<Integer> l = new ArrayList<>(m.keySet());
        l.remove(Integer.valueOf(1));

        log.info(l.toString());
    }
}
