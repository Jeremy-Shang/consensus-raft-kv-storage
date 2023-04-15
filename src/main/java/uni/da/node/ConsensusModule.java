package uni.da.node;
/*
    共识模块
        - 负责和其他共识模块进行通信
        - 判断写入Log的内容
*/

import uni.da.entity.RequestVoteRespond;

public interface ConsensusModule extends RaftModule{

    // 要求投票
    RequestVoteRespond requestVote();

    // 反馈投票
    void respondVote(RequestVoteRespond requestVoteRespond);

    String rpcTest();
}
