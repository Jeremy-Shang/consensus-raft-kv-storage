package uni.da.remote.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uni.da.node.Character;
import uni.da.node.NodeParam;
import uni.da.remote.RaftRpcService;
import uni.da.entity.AppendEntryRequest;
import uni.da.entity.RequestVoteRequest;
import uni.da.entity.AppendEntryResponse;
import uni.da.entity.RequestVoteResponse;
import uni.da.util.LogUtil;

import java.io.IOException;

@Slf4j
@Data
public class RaftRpcServiceImpl implements RaftRpcService {

    private NodeParam nodeParam;

    public RaftRpcServiceImpl(NodeParam nodeParam) {
        this.nodeParam = nodeParam;

    }

    @Override
    public RequestVoteResponse requestVote(RequestVoteRequest request) {
        // 如果是自己给自己投 或者 如果自己是follower, 并且对面任期不小于自己，投票
        if (request.getCandidateId() == nodeParam.getId() || (nodeParam.getCharacter() == Character.Follower && request.getTerm() >= nodeParam.getTerm().get())) {

            LogUtil.printBoxedMessage("vote to node " + request.getCandidateId());

            return RequestVoteResponse.builder()
                    .term(nodeParam.getTerm().get())
                    .isVote(true)
                    .build();
        }
        // 否则拒绝投票
        return RequestVoteResponse.builder()
                .term(nodeParam.getTerm().get())
                .isVote(false)
                .build();
    }

    /**
     * 心跳, 追加日志
     * @param request
     * @return
     */
    @Override
    public AppendEntryResponse appendEntry(AppendEntryRequest request) {
        // 收到心跳，状态变更为Follower
        nodeParam.setCharacter(Character.Follower);
        // 维持心跳
        try {
            this.nodeParam.getPipe().getOutputStream().write(1);
        } catch (IOException e) {
            log.error("写入管道失败");
        }


        // TODO 追加日志 同步

        // 1. 加入logbody

        // 2.

        // 3.






        return null;
    }


    @Override
    public void sayHi(NodeParam nodeParam) {
        log.warn(nodeParam.getName() + " say hi to you from " + nodeParam.getAddr().getIp()+ ":" + nodeParam.getAddr().getPort());
    }
}
