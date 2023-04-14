package uni.da.node;
/*
    共识模块
        - 负责和其他共识模块进行通信
        - 判断写入Log的内容
*/

public interface ConsensusModule extends RaftModule{

    void requestVoteRpcRequest();

    void requestVoteRpcRespond();

    void appendEntriesRpcRequest();

    void appendEntriesRpcRespond();

}
