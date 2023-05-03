package uni.da.task;


import lombok.extern.slf4j.Slf4j;
import uni.da.node.Character;
import uni.da.node.ConsensusState;
import uni.da.statemachine.fsm.component.Event;
import uni.da.statemachine.fsm.component.EventType;

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
     * 以写入管道的方式来判断是否有心跳到达
     * @return
     * @throws Exception
     */
    @Override
    public EventType call() throws Exception {

        try {
            int signal = this.consensusState.getPipe().getInputStream().read();
            // 监听到心跳，角色变为Follower
            consensusState.setCharacter(Character.Follower);
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
