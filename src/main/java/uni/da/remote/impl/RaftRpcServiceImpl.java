package uni.da.remote.impl;

import com.sun.org.apache.xerces.internal.impl.xs.identity.UniqueOrKey;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uni.da.common.NodeParam;
import uni.da.node.Node;
import uni.da.remote.RaftRpcService;
import uni.da.entity.AppendEntryRequest;
import uni.da.entity.RequestVoteRequest;
import uni.da.entity.AppendEntryResponse;
import uni.da.entity.RequestVoteResponse;

import java.io.IOException;
import java.io.PipedOutputStream;

@Slf4j
@Data
public class RaftRpcServiceImpl implements RaftRpcService {


    private NodeParam nodeParam;

    public RaftRpcServiceImpl(NodeParam nodeParam) {
        this.nodeParam = nodeParam;

    }

    @Override
    public RequestVoteResponse requestVote(RequestVoteRequest request) {
        return null;
    }


    /**
     * 心跳, 追加日志
     * @param request
     * @return
     */
    @Override
    public AppendEntryResponse appendEntry(AppendEntryRequest request) {
        // 维持心跳
        try {
            this.nodeParam.getPipe().getOutputStream().write(1);
        } catch (IOException e) {
            log.error("写入管道失败=");
        }

        // TODO 其他工作

        return null;
    }


    @Override
    public void sayHi(NodeParam nodeParam) {
        log.warn(nodeParam.getName() + " say hi to you from " + nodeParam.getAddr().getIp()+ ":" + nodeParam.getAddr().getPort());
    }
}
