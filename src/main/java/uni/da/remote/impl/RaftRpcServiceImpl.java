package uni.da.remote.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uni.da.entity.*;
import uni.da.entity.Log.LogBody;
import uni.da.entity.Log.LogEntry;
import uni.da.node.Character;
import uni.da.node.ConsensusState;
import uni.da.remote.RaftRpcService;
import uni.da.statemachine.fsm.component.Event;
import uni.da.statemachine.fsm.component.EventType;
import uni.da.task.BroadcastTask;
import uni.da.util.LogUtil;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
@Data
public class RaftRpcServiceImpl extends UnicastRemoteObject implements RaftRpcService {

    private ConsensusState consensusState;

    public RaftRpcServiceImpl(ConsensusState consensusState) throws RemoteException {
        super();
        this.consensusState = consensusState;

    }

    /**
     * 判断是否投票
     * @param request
     * @return
     */
    @Override
    public RequestVoteResponse requestVote(RequestVoteRequest request) {
        RequestVoteResponse reject = RequestVoteResponse.builder()
                .term(consensusState.getTerm().get())
                .isVote(false)
                .build();

        // 如果当前的请求投票中的任期已经投过了，拒绝
        /**
         * TODO 如果投过票的人，在同一个任期还向我要票，我还是要给
         */
        if (consensusState.getVoteHistory().containsKey(request.getTerm())) {

            LogUtil.printBoxedMessage("reject to vote node (already vote)" + request.getCandidateId() + " current term: " + consensusState.getTerm().get());

            return reject;
        }

        // 如果是自己给自己投 或者 如果自己是follower, 并且对面任期不小于自己，投票
        if (request.getCandidateId() == consensusState.getId() || (consensusState.getCharacter() == Character.Follower && request.getTerm() >= consensusState.getTerm().get())) {

            LogUtil.printBoxedMessage("vote to node " + request.getCandidateId() + " current term: " + consensusState.getTerm().get());

            /** 加入投票历史，表示在这一轮投了谁 */
            consensusState.getVoteHistory().put(consensusState.getTerm().get(), request.getCandidateId());

            return RequestVoteResponse.builder()
                    .term(consensusState.getTerm().get())
                    .isVote(true)
                    .build();
        }

        LogUtil.printBoxedMessage("reject to vote node (condition not satisfied)" + request.getCandidateId() + " current term: " + consensusState.getTerm().get());

        // 否则拒绝投票
        return reject;
    }

    /**
     * 心跳和追加日志逻辑
     *
     * @param request
     * @return
     */
    @Override
    public AppendEntryResponse appendEntry(AppendEntryRequest request) {
        /** 维持心跳的工作 */
        // 1. 任期号跟Leader同步
        consensusState.getTerm().set(request.getTerm());

        // 2. 状态变更为Follower
        consensusState.setCharacter(Character.Follower);

        // 3. TODO 设置当前leaderID
        consensusState.getLeaderId().getAndSet(request.getLeaderId());


        // 3. 解除阻塞，继续监听心跳
        try {
            this.consensusState.getPipe().getOutputStream().write(1);
        } catch (IOException e) {
            log.error("写入管道失败");
        }

        // TODO 追加日志 同步
        // 1. 一致性检查, 当前日志体中不包含prevIndex，拒绝
        boolean isPresent = consensusState
                .getLogModule()
                .isPresent(request.getPrevLogIndex());

        if (!isPresent) {
            return AppendEntryResponse.builder()
                    .term(consensusState.getTerm().get())
                    .isSuccess(false)
                    .build();
        }


        // 2. 追加日志
        // 1. 如果是心跳消息 (空日志体), 跟leader汇报自己目前match到哪里就可以了
        if (request.getLogEntry() == null) {
            return AppendEntryResponse.builder()
                    .isHeartBeat(true)
                    .matchIndex(consensusState.getLogModule().getLastLogIndex())
                    .term(consensusState.getTerm().get())
                    .isSuccess(true)
                    .build();
        } else {
            consensusState.getLogModule().append(request.getLogEntry());

            // 加入leader给的日志体之后，matchIndex会发生变化
            return AppendEntryResponse.builder()
                    .term(consensusState.getTerm().get())
                    .isSuccess(true)
                    .isHeartBeat(false)
                    .matchIndex(consensusState.getLogModule().getLastLogIndex())
                    .build();

        }

    }

    @Override
    public ClientResponse handleClient(ClientRequest request) throws ExecutionException, InterruptedException {

        log.info(request.toString());

        // 1. TODO get request
        if (request.getTYPE() == 1) {
            return null;
        }


        int key = request.getKey();
        String val = request.getVal();

        // 2. TODO 重定向到leader
        if (consensusState.getCharacter() != Character.Leader) {
            return null;
        }

        // 2. 当前leader中直接插入指令
        LogEntry logEntry = LogEntry.builder()
                .term(consensusState.getTerm().get())
                .logIndex(consensusState.getLogModule().getLastLogIndex() + 1)
                .body(new LogBody(key ,val))
                .build();

        consensusState.getLogModule().append(logEntry);

        // 3. 发起消息广播
        Future<EventType> future =  consensusState.getNodeExecutorService().submit(new BroadcastTask(consensusState));

        try {
            future.get();
        } catch (Exception e) {
            log.error("任务失败");
        }


        // 4. 客户端回显数据
        Map<Integer, List<LogEntry>> clientEcho = new HashMap<>();

        for(Integer k: consensusState.getRemoteServiceMap().keySet()) {

            RaftRpcService s = consensusState.getRemoteServiceMap().get(k);

            CopyOnWriteArrayList<LogEntry> logEntries = null;
            try {
                logEntries = s.gatherClusterLogEntries();
            } catch (Exception e) {
                log.error("获取节点日志失败");
            }
            clientEcho.put(k, new ArrayList<>(logEntries));
        }


        // 4. TODO 客户端需要回显
        return ClientResponse.success(clientEcho);
    }


    /**
     * 客户端回显，获取所有节点log状态
     * @return
     */
    @Override
    public CopyOnWriteArrayList<LogEntry> gatherClusterLogEntries() {
        return consensusState.getLogModule().getLogEntries();
    }




    @Override
    public void sayHi() {
        log.info("hi");
    }
}
