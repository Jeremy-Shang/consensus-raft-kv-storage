package uni.da.task;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import uni.da.entity.AppendEntryResponse;
import uni.da.entity.Log.LogEntry;
import uni.da.node.Character;
import uni.da.node.ConsensusState;
import uni.da.entity.AppendEntryRequest;
import uni.da.remote.RaftRpcService;
import uni.da.statemachine.fsm.component.EventType;

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

//        LogUtil.printBoxedMessage(consensusState.getName() + " broadcast msg !");

        CopyOnWriteArrayList<AppendEntryResponse> responses = new CopyOnWriteArrayList<>();

        Map<Integer, RaftRpcService> remoteServiceMap = this.consensusState.getRemoteServiceMap();

        // 过滤广播目标，不给自己广播心跳
        Set<Integer> keysExcludeSelf = remoteServiceMap.keySet().stream()
                .filter(e -> e != this.consensusState.getId())
                .collect(Collectors.toSet());

        // 睡一会，避免疯狂心跳
        Thread.sleep(consensusState.getTimeout() / 10);

        // 并发广播心跳
        for(Integer sid: keysExcludeSelf) {
            consensusState.getNodeExecutorService().execute(new Runnable() {
                @Override
                public void run() {

                    // 当前节点 "期待" 的下一条日志
                    int nextLogIndex = consensusState.getNextIndex().get(sid);

                    // 可能为空 (心跳)
                    LogEntry logEntry = consensusState.getLogModule().getEntryByIndex(nextLogIndex);

                    // 当前节点到目前为止已经ack的日志索引和任期
                    int prevLogIndex = consensusState.getMatchIndex().get(sid);
                    int preLogTerm = consensusState.getLogModule().getEntryByIndex(prevLogIndex).getTerm();

                    // 构造广播消息
                    AppendEntryRequest request = AppendEntryRequest.builder()
                            .term(consensusState.getCurrTerm().get())           // 当前任期
                            .leaderId(consensusState.getId())               // leader id
                            .prevLogIndex(prevLogIndex)
                            .preLogTerm(preLogTerm)
                            .logEntry(logEntry)                             // 可能为null, 如果为null，对面会回应这是心跳
                            .leaderCommit(consensusState.getLogModule().getMaxCommit())
                            .build();

                    try {
                        AppendEntryResponse response = remoteServiceMap.get(sid).appendEntry(request);

                        if (response.isSuccess() && !response.isHeartBeat()) {
                            // 更新该节点目前的match Index
                            consensusState.getMatchIndex().put(sid, response.getMatchIndex());
                            // 更新该节点期待的下一个 Index
                            consensusState.getNextIndex().put(sid, response.getMatchIndex() + 1);
                        }

                        responses.add(response);

                    } catch (Exception e) {
                        log.error("发送心跳消息失败：{} -> {} ", consensusState.getId(), sid);
                    }
                }
            });
        }

        // 4. 如果过半被复制，则commit. 统计过半matchindex
        List<Integer> commitIndexes = responses.stream()
                .filter(e -> e.isSuccess())
                .collect(Collectors.groupingBy(AppendEntryResponse::getMatchIndex, Collectors.counting()))
                .entrySet().stream()
                .filter(e -> e.getValue() > consensusState.getClusterAddr().size() / 2)
                .map(e -> e.getKey())
                .collect(Collectors.toList());

        // 5. leader进行commit
        commitIndexes.forEach(i -> consensusState.getLogModule().apply(i));


        return EventType.SUCCESS;
    }
}
