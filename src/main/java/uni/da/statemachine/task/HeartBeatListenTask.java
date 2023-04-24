package uni.da.statemachine.task;


import lombok.extern.slf4j.Slf4j;
import uni.da.common.Pipe;
import uni.da.statemachine.fsm.component.EventType;

import java.util.concurrent.Callable;

@Slf4j
public class HeartBeatListenTask implements Callable<EventType> {
    private Pipe pipe;

    public HeartBeatListenTask(Pipe pipe) {
        this.pipe = pipe;
    }
    @Override
    public EventType call() throws Exception {
        try {
            int signal = pipe.getInputStream().read();
            return EventType.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return EventType.FAIL;
        }
    }
}
