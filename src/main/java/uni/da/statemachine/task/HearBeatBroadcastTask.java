package uni.da.statemachine.task;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import uni.da.node.Character;
import uni.da.node.NodeParam;
import uni.da.entity.AppendEntryRequest;
import uni.da.remote.RaftRpcService;
import uni.da.statemachine.fsm.component.EventType;
import uni.da.util.LogUtil;

@Slf4j
public class HearBeatBroadcastTask extends AbstractRaftTask {


    public HearBeatBroadcastTask(NodeParam nodeParam) {
        super(nodeParam);
    }

    @Override
    public EventType call() throws Exception {
        // 只有leader才能广播心跳
        if (nodeParam.getCharacter() != Character.Leader) {
            return EventType.FAIL;
        }

        LogUtil.printBoxedMessage(nodeParam.getName() + " start hearbeat !");

        Map<Integer, RaftRpcService> remoteServiceMap = this.nodeParam.getRemoteServiceMap();

        // 过滤广播目标，不给自己广播心跳
        Set<Integer> keysExcludeSelf = remoteServiceMap.keySet().stream()
                .filter(e -> e != this.nodeParam.getId())
                .collect(Collectors.toSet());

        // 睡一会，避免疯狂心跳
        Thread.sleep(nodeParam.getTimeout() / 2);

        // 并发广播心跳
        for(Integer id: keysExcludeSelf) {
            nodeParam.getNodeExecutorService().execute(new Runnable() {
                @Override
                public void run() {
                    // 构造广播消息
                    AppendEntryRequest heartBeat = AppendEntryRequest.builder()
                            .term(nodeParam.getTerm().get())
                            .leaderId(nodeParam.getId())
                            .prevLogIndex(nodeParam.getLogModule().getPrevLogIndex())
                            .preLogTerm(nodeParam.getLogModule().getPrevLogTerm())
                            .logEntries(nodeParam.getLogModule().getEmptyLogEntries())
                            .leaderCommit(nodeParam.getLogModule().getMaxCommit())
                            .build();
                    try {
                        remoteServiceMap.get(id).appendEntry(heartBeat);
                    } catch (Exception e) {
                        log.error("发送心跳消息失败：{} -> {} ", nodeParam.getId(), id);
                        e.printStackTrace();
                    }
                }
            });
        }

        return EventType.SUCCESS;
    }
}
