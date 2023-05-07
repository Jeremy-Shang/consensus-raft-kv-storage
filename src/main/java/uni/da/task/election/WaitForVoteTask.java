package uni.da.task.election;

import lombok.extern.slf4j.Slf4j;
import uni.da.entity.RequestVoteRequest;
import uni.da.entity.RequestVoteResponse;
import uni.da.node.Character;
import uni.da.node.ConsensusState;
import uni.da.remote.RaftRpcService;
import uni.da.statetransfer.fsm.component.EventType;
import uni.da.task.AbstractRaftTask;
import uni.da.util.LogType;
import uni.da.util.LogUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 *  等待选票线程
 */
@Slf4j
class WaitForVoteTask extends AbstractRaftTask implements Callable<EventType> {
    Object sign;

    CountDownLatch latch;

    public WaitForVoteTask(ConsensusState consensusState, Object sign, CountDownLatch latch) {
        super(consensusState);

        this.sign = sign;

        this.latch = latch;
    }

    /**
     * On conversion to candidate, start election:
     *      • Increment currentTerm
     *      • Vote for self
     *      • Reset election timer
     *      • Send RequestVote RPCs to all other servers
     *      • If votes received from the majority of servers: become leader
     *      • If AppendEntries RPC received from new leader: convert to
     *          follower
     *      • If election timeout elapses: start new election
     * @return
     * @throws Exception
     */
    @Override
    public EventType call() throws Exception {

        consensusState.retryConnection();

        int requiredVotes = (consensusState.getClusterSize()/2) + 1;

        /** Convert to candidate */
        consensusState.setCharacter(Character.Candidate);

        /** Increment currentTerm */
        consensusState.getCurrTerm().incrementAndGet();

        /** Vote for self */
        consensusState.setVotedFor(consensusState.getId());
        requiredVotes -= 1;

        log.info("[{}] {} {} start election ! curr term {}", LogType.START_ELECTION, consensusState.getCharacter(), consensusState.getName(), consensusState.getCurrTerm());


        /** TODO: Reset election timer ? */


        /** (Concurrently) Send RequestVote RPCs to all other servers */


        Map<Integer, RaftRpcService> otherNodesService = new HashMap<>(consensusState.getRemoteServiceMap());

        otherNodesService.remove(consensusState.getId());

        CountDownLatch votesCount = new CountDownLatch(requiredVotes);


        for(Integer sid: otherNodesService.keySet()) {
            RequestVoteRequest requestVoteRequest = RequestVoteRequest.builder()
                    .term(consensusState.getCurrTerm().get())
                    .candidateId(consensusState.getId())
                    .lastLogIndex(consensusState.getLogModule().getLastLogIndex())
                    .lastLogTerm(consensusState.getLogModule().getLastLogTerm())
                    .build();

            consensusState.getNodeExecutorService().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        RequestVoteResponse response = otherNodesService.get(sid).requestVote(requestVoteRequest);
                        if (response.isVoteGranted()) {
                            log.info("[RECEIVE VOTE] node{} receive vote from node{}", consensusState.getId(), sid);
                            votesCount.countDown();
                        }
                    } catch (Exception e) {
                        log.error("[SEND REQUEST VOTE FAIL]：{} -> {} ", consensusState.getId(), sid);
                        consensusState.getCrashNodes().add(sid);
                    }
                }
            });
        }

        votesCount.await();

        // If votes received from the majority of servers: become leader
        log.info("[{}] {} become leader! currTerm {}. ", LogType.CHARACTER_CHANGE, consensusState.getName(), consensusState.getCurrTerm());
        consensusState.setCharacter(Character.Leader);
        consensusState.getLeaderId().set(consensusState.getId());


        sign = "task finish";
        latch.countDown();

        return EventType.SUCCESS;
    }
}