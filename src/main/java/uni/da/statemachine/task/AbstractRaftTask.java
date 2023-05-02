package uni.da.statemachine.task;

import uni.da.node.ConsensusState;
import uni.da.statemachine.fsm.component.EventType;

import java.util.concurrent.Callable;

public abstract class AbstractRaftTask implements Callable<EventType> {

    protected ConsensusState consensusState;

    public AbstractRaftTask(ConsensusState consensusState) {
        this.consensusState = consensusState;
    }
}
