package uni.da.task.election;

import lombok.extern.slf4j.Slf4j;
import uni.da.entity.RequestVoteRequest;
import uni.da.entity.RequestVoteResponse;
import uni.da.node.Character;
import uni.da.node.ConsensusState;
import uni.da.remote.RaftRpcService;
import uni.da.statemachine.fsm.component.Event;
import uni.da.statemachine.fsm.component.EventType;
import uni.da.task.AbstractRaftTask;
import uni.da.task.ListeningTask;
import uni.da.util.LogUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

@Slf4j
public class ElectionTask extends AbstractRaftTask {

    private volatile Object receiveHearBeat = null;
    private volatile Object electionSuccess = null;

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

        /**
         * 2个线程
         * 请求投票线程, 等待心跳线程
         *
         * 1. 请求投票返回成功 -> 主线程成功
         * 2. 请求投票超时 -> 主线程失败
         * 3. 心跳线程如果成功 -> 主线程失败
         */

        CountDownLatch latch = new CountDownLatch(1);

        consensusState.getNodeExecutorService().submit(new WaitForVoteTask(consensusState, electionSuccess, latch));

        consensusState.getNodeExecutorService().submit(new ListeningTask(consensusState, receiveHearBeat, latch));

        latch.await();

        if (receiveHearBeat != null) {
            // 1. 事件一，收到心跳，直接失败, 轮转到follower (监听心跳)
            return EventType.FAIL;
        } else {
            // 2. 事件二，得到选票，直接成功，当选Leader
            return EventType.SUCCESS;
        }

        // 3. 事件三, 超时，重新发起选举
    }
}
