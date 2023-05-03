package uni.da.task;


import lombok.extern.slf4j.Slf4j;
import uni.da.node.Character;
import uni.da.node.ConsensusState;
import uni.da.statemachine.fsm.component.Event;
import uni.da.statemachine.fsm.component.EventType;

@Slf4j
public class ListeningTask extends AbstractRaftTask{

    public ListeningTask(ConsensusState consensusState) {
        super(consensusState);
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
            return EventType.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return EventType.FAIL;
        }
    }
}
