package uni.da.statemachine.task;


import lombok.extern.slf4j.Slf4j;
import uni.da.common.NodeParam;
import uni.da.common.Pipe;
import uni.da.statemachine.fsm.component.EventType;

import java.util.concurrent.Callable;

@Slf4j
public class HeartBeatListenTask extends AbstractRaftTask{

    public HeartBeatListenTask(NodeParam nodeParam) {
        super(nodeParam);
    }

    @Override
    public EventType call() throws Exception {
        try {
            int signal = this.nodeParam.getPipe().getInputStream().read();
            return EventType.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return EventType.FAIL;
        }
    }
}
