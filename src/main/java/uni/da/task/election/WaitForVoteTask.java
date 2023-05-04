package uni.da.task.election;

import lombok.extern.slf4j.Slf4j;
import uni.da.entity.RequestVoteRequest;
import uni.da.entity.RequestVoteResponse;
import uni.da.node.Character;
import uni.da.node.ConsensusState;
import uni.da.remote.RaftRpcService;
import uni.da.statemachine.fsm.component.EventType;
import uni.da.task.AbstractRaftTask;
import uni.da.util.LogUtil;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 *  等待选票线程
 */
@Slf4j
class WaitForVoteTask extends AbstractRaftTask implements Callable<EventType> {

    Object sign;

    CountDownLatch latch;

    public WaitForVoteTask(ConsensusState consensusState, Object sign, CountDownLatch latch) {
        super(consensusState);

        this.sign = sign;

        this.latch = latch;
    }


    @Override
    public EventType call() throws Exception {
        // 节点增加自己任期，并进入候选人状态
        consensusState.getCurrTerm().incrementAndGet();
        consensusState.setCharacter(Character.Candidate);
        LogUtil.printBoxedMessage(consensusState.getName() + " start election !");
        Map<Integer, RaftRpcService> remoteServiceMap = this.consensusState.getRemoteServiceMap();

        // 过半数票
        CountDownLatch votes = new CountDownLatch((int) Math.ceil(consensusState.getClusterAddr().size() / 2) + 1);

        // 请求所有人投票，包括自己
        for(Integer id: remoteServiceMap.keySet()) {

            RequestVoteRequest requestVoteRequest = RequestVoteRequest.builder()
                    .term(consensusState.getCurrTerm().get())
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
                        if (response.isVoteGranted()) {
                            log.info(" ------> Get vote from: " + "Node " + id);
                            votes.countDown();
                        }
                    } catch (Exception e) {
                        log.error("发送请求投票消息失败：{} -> {} ", consensusState.getId(), id);
                    }
                }
            });
        }


        votes.await();
        LogUtil.printBoxedMessage(consensusState.getName() + " become leader !");
        consensusState.setCharacter(Character.Leader);

        // 主线程解除阻塞
        sign = "task finish";
        latch.countDown();

        return EventType.SUCCESS;
    }
}