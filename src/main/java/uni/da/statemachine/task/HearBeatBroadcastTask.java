package uni.da.statemachine.task;

import java.util.Map;

import uni.da.common.NodeParam;
import uni.da.entity.AppendEntryRequest;
import uni.da.remote.RaftRpcService;
import uni.da.statemachine.fsm.component.EventType;

public class HearBeatBroadcastTask extends AbstractRaftTask {


    public HearBeatBroadcastTask(NodeParam nodeParam) {
        super(nodeParam);
    }

    @Override
    public EventType call() throws Exception {
        Map<Integer, RaftRpcService> remoteServiceMap = this.nodeParam.getRemoteServiceMap();
        Thread.sleep(3000);
        // 广播心跳
        try {
            for(Integer id: remoteServiceMap.keySet()) {

                AppendEntryRequest heartBeat = AppendEntryRequest.builder()
                        .term(this.nodeParam.getTerm().get())
                        .leaderId(this.nodeParam.getId())
                        .prevLogIndex(this.nodeParam.getLogModule().getPrevLogIndex())
                        .preLogTerm(this.nodeParam.getLogModule().getPrevLogTerm())
                        .logEntries(this.nodeParam.getLogModule().getEmptyLogEntries())
                        .leaderCommit(this.nodeParam.getLogModule().getMaxCommit())
                        .build();

                remoteServiceMap.get(id).appendEntry(heartBeat);
            }
        } catch (Exception e) {
            return EventType.FAIL;
        }
        return EventType.SUCCESS;
    }
}
