package uni.da.statemachine.task;

import lombok.extern.slf4j.Slf4j;
import uni.da.statemachine.fsm.component.EventType;

import java.util.concurrent.Callable;

@Slf4j
public class ElectionTask implements Callable<EventType> {

    @Override
    public EventType call() throws Exception {

        return EventType.SUCCESS;
    }
}
