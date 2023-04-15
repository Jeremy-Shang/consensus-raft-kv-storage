package uni.da.remote;

/*
    远程服务接口, Raft集群每个节点负责处理的rpc call有三类
        - 回复 appendlogentries
        - 回复 requestvote
        - 接受心跳
 */
public interface RaftRpcService {

    public String requestVote(String name);

    public String appendLog();

    public String heartBeat();
}
