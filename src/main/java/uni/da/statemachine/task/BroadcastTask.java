package uni.da.statemachine.task;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import uni.da.entity.AppendEntryResponse;
import uni.da.entity.Log.LogBody;
import uni.da.entity.Log.LogEntry;
import uni.da.node.Character;
import uni.da.node.ConsensusState;
import uni.da.entity.AppendEntryRequest;
import uni.da.remote.RaftRpcService;
import uni.da.statemachine.fsm.component.EventType;
import uni.da.util.LogUtil;

@Slf4j
public class BroadcastTask extends AbstractRaftTask {

    private LogEntry logEntry = null;

    public BroadcastTask(ConsensusState consensusState) {
        super(consensusState);
    }


    @Override
    public EventType call() throws Exception {
        // 只有leader才能广播心跳
        if (consensusState.getCharacter() != Character.Leader) {
            return EventType.FAIL;
        }

        LogUtil.printBoxedMessage(consensusState.getName() + " start hearbeat !");

        Map<Integer, RaftRpcService> remoteServiceMap = this.consensusState.getRemoteServiceMap();

        // 过滤广播目标，不给自己广播心跳
        Set<Integer> keysExcludeSelf = remoteServiceMap.keySet().stream()
                .filter(e -> e != this.consensusState.getId())
                .collect(Collectors.toSet());

        // 睡一会，避免疯狂心跳
        Thread.sleep(consensusState.getTimeout() / 2);

        // 并发广播心跳
        for(Integer id: keysExcludeSelf) {
            consensusState.getNodeExecutorService().execute(new Runnable() {
                @Override
                public void run() {
                    // 构造广播消息

                    int prevLogIndex = consensusState.getMatchIndex().get(id);
                    int preLogTerm = consensusState.getLogModule().getEntryByIndex(prevLogIndex).getTerm();
                    int nextLogIndex = consensusState.getNextIndex().get(id);

                    AppendEntryRequest request = AppendEntryRequest.builder()
                            .term(consensusState.getTerm().get())
                            .leaderId(consensusState.getId())
                            // 发送对应节点已经同步的最高日志索引
                            .prevLogIndex(prevLogIndex)
                            // 发送该索引对应日志的任期
                            .preLogTerm(preLogTerm)
                            // 当前应该发送的日志体
                            .logEntry(consensusState.getLogModule().getEntryByIndex(nextLogIndex))
                            .leaderCommit(consensusState.getLogModule().getMaxCommit())
                            .build();

                    try {
                        AppendEntryResponse response = remoteServiceMap.get(id).appendEntry(request);
                        // 如果成功，match index等于
                        if (response.isSuccess()) {
                            consensusState.getMatchIndex().put(id, prevLogIndex);
                            consensusState.getNextIndex().put(id, prevLogIndex + 1);
                        }

                    } catch (Exception e) {
                        log.error("发送心跳消息失败：{} -> {} ", consensusState.getId(), id);
                    }
                }
            });
        }



        return EventType.SUCCESS;
    }
}
