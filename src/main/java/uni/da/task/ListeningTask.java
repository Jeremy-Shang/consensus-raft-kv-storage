package uni.da.task;


import lombok.extern.slf4j.Slf4j;
import uni.da.node.ConsensusState;
import uni.da.statetransfer.fsm.component.EventType;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class ListeningTask extends AbstractRaftTask{

    Object sign;

    CountDownLatch latch;

    public ListeningTask(ConsensusState consensusState) {
        super(consensusState);
    }

    public ListeningTask(ConsensusState consensusState, Object sign, CountDownLatch latch) {
        super(consensusState);
        this.sign = sign;
        this.latch = latch;
    }

    /**
     * Using pipe's blocking reading as timer.
     * @return
     * @throws Exception
     */
    @Override
    public EventType call() throws Exception {
        try {
            int signal = this.consensusState.getTimer().getInputStream().read();
            if (latch != null) {
                sign = "wakeup";
                latch.countDown();
            }
            return EventType.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return EventType.FAIL;
        }
    }
}
