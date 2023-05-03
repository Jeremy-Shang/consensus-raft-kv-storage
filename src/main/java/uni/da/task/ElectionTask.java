package uni.da.task;

import lombok.extern.slf4j.Slf4j;
import uni.da.entity.RequestVoteRequest;
import uni.da.entity.RequestVoteResponse;
import uni.da.node.Character;
import uni.da.node.ConsensusState;
import uni.da.remote.RaftRpcService;
import uni.da.statemachine.fsm.component.Event;
import uni.da.statemachine.fsm.component.EventType;
import uni.da.util.LogUtil;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ElectionTask extends AbstractRaftTask {

    public ElectionTask(ConsensusState consensusState) {
        super(consensusState);
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
        consensusState.getTerm().incrementAndGet();


        consensusState.setCharacter(Character.Candidate);


        LogUtil.printBoxedMessage(consensusState.getName() + " start election !");









        Map<Integer, RaftRpcService> remoteServiceMap = this.consensusState.getRemoteServiceMap();


        // 过半数票
        CountDownLatch votes = new CountDownLatch((int) Math.ceil(consensusState.getClusterAddr().size() / 2) + 1);



        // 请求所有人投票，包括自己
        for(Integer id: remoteServiceMap.keySet()) {
            RequestVoteRequest requestVoteRequest = RequestVoteRequest.builder()
                    .term(consensusState.getTerm().get())
                    .candidateId(consensusState.getId())
                    .lastLogIndex(consensusState.getLogModule().getLastLogIndex())
                    .lastLogTerm(consensusState.getLogModule().getLastLogTerm())
                    .build();

            // 并发执行投票任务
            consensusState.getNodeExecutorService().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        RequestVoteResponse response = remoteServiceMap.get(id).requestVote(requestVoteRequest);
                        if (response.isVote()) {
                            votes.countDown();
                        }
                    } catch (Exception e) {
                        log.error("发送请求投票消息失败：{} -> {} ", consensusState.getId(), id);
//                        e.printStackTrace();
                    }
                }
            });
        }

        // 所有人都投票，则通过
        votes.await();

        // // 如果收到别的心跳，即被变更了角色，则失败
        if (consensusState.getCharacter() != Character.Candidate) {
            return EventType.FAIL;
        }
        LogUtil.printBoxedMessage(consensusState.getName() + " become leader !");

        consensusState.setCharacter(Character.Leader);

        return EventType.SUCCESS;

    }
}
