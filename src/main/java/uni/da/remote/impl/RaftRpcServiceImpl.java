package uni.da.remote.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uni.da.entity.Log.LogBody;
import uni.da.entity.Log.LogEntry;
import uni.da.node.Character;
import uni.da.node.ConsensusState;
import uni.da.remote.RaftRpcService;
import uni.da.entity.AppendEntryRequest;
import uni.da.entity.RequestVoteRequest;
import uni.da.entity.AppendEntryResponse;
import uni.da.entity.RequestVoteResponse;
import uni.da.statemachine.task.BroadcastTask;
import uni.da.util.LogUtil;

import java.io.IOException;

@Slf4j
@Data
public class RaftRpcServiceImpl implements RaftRpcService {

    private ConsensusState consensusState;

    public RaftRpcServiceImpl(ConsensusState consensusState) {
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

        // 1. 任期号跟Leader同步
        consensusState.getTerm().set(request.getTerm());

        // 2. 状态变更为Follower
        consensusState.setCharacter(Character.Follower);

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

        // 2. 加入logbody, 返回成功
        consensusState.getLogModule().append(request.getLogEntry());

        return AppendEntryResponse.builder()
                .term(consensusState.getTerm().get())
                .isSuccess(true)
                .build();
    }

    public String handleClientRequest(int key, String val) {

        // 1. TODO redirect to leader
        if (consensusState.getCharacter() != Character.Leader) {
            return "";
        }

        // 2. 当前leader中直接插入指令
        LogEntry logEntry = LogEntry.builder()
                .term(consensusState.getTerm().get())
                .logIndex(consensusState.getLogModule().getLastLogIndex() + 1)
                .body(new LogBody(key ,val))
                .build();

        consensusState.getLogModule().append(logEntry);

        // 3. 发起消息广播
        consensusState.getNodeExecutorService().submit(new BroadcastTask(consensusState));








        // 4. 收到半数/没收到半数, 需要进行commit


        return null;
    }






    @Override
    public void sayHi(ConsensusState consensusState) {
        log.warn(consensusState.getName() + " say hi to you from " + consensusState.getAddr().getIp()+ ":" + consensusState.getAddr().getPort());
    }
}
