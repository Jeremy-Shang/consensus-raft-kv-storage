package uni.da.statemachine.task;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import uni.da.entity.AppendEntryRequest;
import uni.da.entity.RequestVoteRequest;
import uni.da.entity.RequestVoteResponse;
import uni.da.node.Character;
import uni.da.node.NodeParam;
import uni.da.remote.RaftRpcService;
import uni.da.statemachine.fsm.component.EventType;
import uni.da.util.LogUtil;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ElectionTask extends AbstractRaftTask {

    public ElectionTask(NodeParam nodeParam) {
        super(nodeParam);
    }

    /**
     * 发起选举
     *  1. 得到半数投票 -> 成功
     *  2. 得不到半数投票 -> 阻塞，直到超时失败
     *  3. 得到其他的心跳 -> 选举失败
     * @return
     * @throws Exception
     */
    @Override
    public EventType call() throws Exception {
        // 节点增加自己任期，并进入候选人状态
        nodeParam.getTerm().incrementAndGet();
        nodeParam.setCharacter(Character.Candidate);

        LogUtil.printBoxedMessage(nodeParam.getName() + " start election !");

        Map<Integer, RaftRpcService> remoteServiceMap = this.nodeParam.getRemoteServiceMap();

        // 过半数票
        CountDownLatch votes = new CountDownLatch((int) Math.ceil(nodeParam.getClusterAddr().size() / 2) + 1);

        // 请求所有人投票，包括自己
        for(Integer id: remoteServiceMap.keySet()) {
            RequestVoteRequest requestVoteRequest = RequestVoteRequest.builder()
                    .term(nodeParam.getTerm().get())
                    .candidateId(nodeParam.getId())
                    .lastLogIndex(nodeParam.getLogModule().getLastLogIndex())
                    .lastLogTerm(nodeParam.getLogModule().getLastLogTerm())
                    .build();

            // 并发执行投票任务
            nodeParam.getNodeExecutorService().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        RequestVoteResponse response = remoteServiceMap.get(id).requestVote(requestVoteRequest);
                        if (response.isVote()) {
                            votes.countDown();
                        }
                    } catch (Exception e) {
                        log.error("发送请求投票消息失败：{} -> {} ", nodeParam.getId(), id);
//                        e.printStackTrace();
                    }
                }
            });
        }

        // 所有人都投票，则通过
        votes.await();

        // // 如果收到别的心跳，即被变更了角色，则失败
        if (nodeParam.getCharacter() != Character.Candidate) {
            return EventType.FAIL;
        }
        LogUtil.printBoxedMessage(nodeParam.getName() + " become leader !");

        nodeParam.setCharacter(Character.Leader);

        return EventType.SUCCESS;

    }
}
