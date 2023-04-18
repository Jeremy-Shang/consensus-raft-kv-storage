package uni.da.node;
/*
    共识模块
        - 负责和其他共识模块进行通信
        - 判断写入Log的内容
*/


import uni.da.remote.RaftRpcClient;
import uni.da.remote.RaftRpcService;

import java.util.Map;

public interface ConsensusModule extends RaftModule{

    // 获取本地暴露服务以及远程提供的服务
    public RaftRpcClient getRpcClient();

    public RaftRpcService getSelfRpcService();

    // 设置远程服务
    public void setRemoteRpcServices(Map<String, RaftRpcService> remoteServiceMap);

    public String sayHi();
}
