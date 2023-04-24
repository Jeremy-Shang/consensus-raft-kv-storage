package uni.da.statemachine.task;

import java.util.Random;
import uni.da.statemachine.fsm.component.EventType;

import java.util.concurrent.Callable;

public class HearBeatBroadcastTask implements Callable<EventType> {
    @Override
    public EventType call() throws Exception {
        Random random = new Random();
        int min = 2000;
        int max = 5000;
        int num = random.nextInt(max - min + 1) + min;

        Thread.sleep(num);
        return EventType.SUCCESS;
    }
}
