package uni.da.statetransfer;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uni.da.node.ConsensusState;
import uni.da.statetransfer.fsm.impl.StateMachineFactory;
import uni.da.statetransfer.fsm.component.Context;
import uni.da.statetransfer.fsm.component.Event;
import uni.da.statetransfer.fsm.component.EventType;
import uni.da.statetransfer.fsm.StateMachine;
import uni.da.task.election.ElectionTask;
import uni.da.task.BroadcastTask;
import uni.da.task.ListeningTask;
import uni.da.statetransfer.fsm.component.RaftState;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Data
public class ServerStateTransfer implements Runnable {

    private Map<RaftState, Callable<EventType>> taskMap = new ConcurrentHashMap<>();

    private StateMachine<RaftState, EventType, Event> stateMachine;

    private RaftState raftState = RaftState.LISTENING_HEARTBEAT;

    private ConsensusState consensusState;


    public ServerStateTransfer(ConsensusState consensusState) throws IOException {

        this.consensusState = consensusState;

        taskMap.put(RaftState.LISTENING_HEARTBEAT, new ListeningTask(consensusState));

        taskMap.put(RaftState.ELECTION, new ElectionTask(consensusState));

        taskMap.put(RaftState.HEAR_BEAT, new BroadcastTask(consensusState));

        stateTransferRegistry();
    }


    /**
     *
     */
    @Override
    public void run() {

        Callable<EventType> currTask = taskMap.get(this.raftState);

        while (!Thread.currentThread().isInterrupted()) {
            log.info("[STATE MACHINE FLOW] 当前状态: {}, 当前任务: {}, 当前任期: {}" , stateMachine.getCurrentState().toString(), currTask.getClass().getName(), consensusState.getCurrTerm());

            Future<EventType> future = consensusState.getNodeExecutorService().submit(currTask);
            EventType futureEventType = EventType.FAIL;

            try {
                futureEventType = future.get(consensusState.getTimeout(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                log.info("======> task {} timeout at: {}ms term {}", currTask.getClass().getName(), consensusState.getTimeout(), consensusState.getCurrTerm());
                futureEventType = EventType.TIME_OUT;
                e.printStackTrace();
            } catch (InterruptedException e) {
                log.info("======> task {} interrupted term{}", currTask.getClass().getName(), consensusState.getCurrTerm());
                futureEventType = EventType.FAIL;
                e.printStackTrace();
            } catch (ExecutionException e) {
                log.info("======> task {} execution fail term{}", currTask.getClass().getName(), consensusState.getCurrTerm());

                futureEventType = EventType.FAIL;
                e.printStackTrace();
            } finally {
                this.raftState = stateMachine.doTransition(futureEventType, new Event(futureEventType));
                currTask = taskMap.get(this.raftState);
            }
        }
    }


    /*
        状态机状态注册
     */
    private void stateTransferRegistry() {

        StateMachineFactory<Context, RaftState, EventType, Event> stateMachineFactory = new StateMachineFactory<>();

        // 心跳监听 -> 成功 = 心跳监听
        stateMachineFactory.addTransition(RaftState.LISTENING_HEARTBEAT, RaftState.LISTENING_HEARTBEAT, EventType.SUCCESS, (o, e ) -> {
            return RaftState.LISTENING_HEARTBEAT;
        });
        // 心跳监听 -> 失败 = 选举
        stateMachineFactory.addTransition(RaftState.LISTENING_HEARTBEAT, RaftState.ELECTION, EventType.FAIL, (o, e ) -> {
            return RaftState.ELECTION;
        });
        // 心跳监听 -> 超时 = 选举
        stateMachineFactory.addTransition(RaftState.LISTENING_HEARTBEAT, RaftState.ELECTION, EventType.TIME_OUT, (o, e ) -> {
            return RaftState.ELECTION;
        });



        // 选举 -> 成功 = 心跳
        stateMachineFactory.addTransition(RaftState.ELECTION, RaftState.HEAR_BEAT, EventType.SUCCESS, (o, e ) -> {
            return RaftState.HEAR_BEAT;
        });
        // 选举 -> 失败 = 心跳监听
        stateMachineFactory.addTransition(RaftState.ELECTION, RaftState.LISTENING_HEARTBEAT, EventType.FAIL, (o, e ) -> {
            return RaftState.LISTENING_HEARTBEAT;
        });
        // 选举 -> 超时 = 选举
        stateMachineFactory.addTransition(RaftState.ELECTION, RaftState.ELECTION, EventType.TIME_OUT, (o, e ) -> {
            return RaftState.ELECTION;
        });



        // 心跳 -> 成功 = 心跳
        stateMachineFactory.addTransition(RaftState.HEAR_BEAT, RaftState.HEAR_BEAT, EventType.SUCCESS, (o, e ) -> {
            return RaftState.HEAR_BEAT;
        });
        // 心跳 -> 失败 = 继续心跳
        stateMachineFactory.addTransition(RaftState.HEAR_BEAT, RaftState.HEAR_BEAT, EventType.FAIL, (o, e ) -> {
            return RaftState.HEAR_BEAT;
        });
        // 心跳 -> 超时 = 继续心跳
        stateMachineFactory.addTransition(RaftState.HEAR_BEAT, RaftState.HEAR_BEAT, EventType.TIME_OUT, (o, e ) -> {
            return RaftState.HEAR_BEAT;
        });


        // 加入初始状态
        stateMachine = stateMachineFactory.make(new Context(), this.raftState);
    }

}
