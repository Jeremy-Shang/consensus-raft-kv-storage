package uni.da.task.election;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import uni.da.node.Character;
import uni.da.node.ConsensusState;
import uni.da.statetransfer.fsm.component.EventType;
import uni.da.task.AbstractRaftTask;
import uni.da.task.ListeningTask;
import uni.da.util.LogType;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class ElectionTask extends AbstractRaftTask {

    private volatile Object signalA = null;
    private volatile Object signalB = null;

    public ElectionTask(ConsensusState consensusState) {
        super(consensusState);
    }


    /**
     * Two threads represent two event:
     * Thread A: If votes received from the majority of servers: become leader
     * Thread B: If AppendEntries RPC received from new leader: convert to
     *           follower
     *
     * if Receive A's signal: this election task success -> leader (heartbeat)
     * if Receive B's signal: this election task fail -> follower (listening heartbeat)
     * if main thread timeout: this election task timeout -> candidate (repeat election)
     * @return
     * @throws Exception
     */
    @Override
    public EventType call() throws Exception {


        CountDownLatch latch = new CountDownLatch(1);

        consensusState.getNodeExecutorService().submit(new WaitForVoteTask(consensusState, signalB, latch));

        consensusState.getNodeExecutorService().submit(new ListeningTask(consensusState, signalA, latch));

        latch.await();

        if (signalA != null) {
            this.consensusState.setCharacter(Character.Follower);
            log.debug("[{}] {} {} get heartbeat from other node. currTerm {}", LogType.CHARACTER_CHANGE, consensusState.getCharacter(), consensusState.getName(), consensusState.getCurrTerm());

            return EventType.FAIL;
        } else {
            return EventType.SUCCESS;
        }
    }
}
