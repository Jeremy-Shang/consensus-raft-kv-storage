package uni.da.statemachine.task;

import uni.da.common.NodeParam;
import uni.da.statemachine.fsm.component.EventType;

import java.util.concurrent.Callable;

public abstract class AbstractRaftTask implements Callable<EventType> {

    protected NodeParam nodeParam;

    public AbstractRaftTask(NodeParam nodeParam) {
        this.nodeParam = nodeParam;
    }
}
