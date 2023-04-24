package uni.da.statemachine.task;

import java.util.Map;

import uni.da.entity.AppendEntryRequest;
import uni.da.remote.RaftRpcService;
import uni.da.statemachine.fsm.component.EventType;

import java.util.concurrent.Callable;

public class HearBeatBroadcastTask implements Callable<EventType> {


    public final Map<String, RaftRpcService> remoteServiceMap;



    public HearBeatBroadcastTask(Map<String, RaftRpcService> remoteServiceMap, AppendEntryRequest appendEntryRequest) {
        this.remoteServiceMap = remoteServiceMap;


    }


    @Override
    public EventType call() throws Exception {
//        Random random = new Random();
//        int min = 2000;
//        int max = 5000;
//        int num = random.nextInt(max - min + 1) + min;
//
//        Thread.sleep(num);

        // 广播心跳
        try {
            for(String k: remoteServiceMap.keySet()) {

            }
        } catch (Exception e) {
            return EventType.FAIL;
        }
        return EventType.SUCCESS;
    }
}
