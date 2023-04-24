package uni.da.statemachine;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uni.da.common.NodeParam;
import uni.da.statemachine.fsm.impl.StateMachineFactory;
import uni.da.statemachine.fsm.component.Context;
import uni.da.statemachine.fsm.component.Event;
import uni.da.statemachine.fsm.component.EventType;
import uni.da.statemachine.fsm.StateMachine;
import uni.da.statemachine.task.ElectionTask;
import uni.da.statemachine.task.HeartBeatListenTask;
import uni.da.statemachine.fsm.component.RaftState;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Data
public class RaftStateMachine implements Runnable {

    private Map<RaftState, Callable<EventType>> taskMap = new ConcurrentHashMap<>();

    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private StateMachine<RaftState, EventType, Event> stateMachine;

    private RaftState raftState = RaftState.LISTENING_HEARTBEAT;

    private NodeParam nodeParam;


    public RaftStateMachine(NodeParam nodeParam) throws IOException {

        this.nodeParam = nodeParam;

        taskMap.put(RaftState.LISTENING_HEARTBEAT, new HeartBeatListenTask(nodeParam));

        taskMap.put(RaftState.ELECTION, new ElectionTask(nodeParam));
//        taskMap.put(State.HEAR_BEAT, new HearBeatBroadcastTask());

        stateTransferRegistry();
    }

    /*
        模型
        - 状态：监听、心跳、选举 -> 监听task、心跳task、选举task
        - 事件：成功、失败
        - 状态机：状态 + 事件 -> 新状态。新状态 -> 新task
        - 执行新task，轮转
     */
    @Override
    public void run() {
        Callable<EventType> currTask = taskMap.get(this.raftState);

        while (!Thread.currentThread().isInterrupted()) {
            log.info("curr state: {}, curr task: {}" , stateMachine.getCurrentState().toString(), currTask.getClass().getName());
            // 提交当前任务到线程池
            Future<EventType> future = executorService.submit(currTask);
            EventType futureEventType = EventType.FAIL;
            try {
                // 更新结果：成功/失败
                futureEventType = future.get(3, TimeUnit.SECONDS);
            } catch (Exception e) {

            } finally {
                log.info("======> task result: {}", futureEventType.toString());
                // 从状态机获得下一个任务
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
        // 选举 -> 成功 = 心跳
        stateMachineFactory.addTransition(RaftState.ELECTION, RaftState.HEAR_BEAT, EventType.SUCCESS, (o, e ) -> {
            return RaftState.HEAR_BEAT;
        });
        // 选举 -> 失败 = 心跳监听
        stateMachineFactory.addTransition(RaftState.ELECTION, RaftState.LISTENING_HEARTBEAT, EventType.FAIL, (o, e ) -> {
            return RaftState.LISTENING_HEARTBEAT;
        });

        // 心跳 -> 成功 = 心跳
        stateMachineFactory.addTransition(RaftState.HEAR_BEAT, RaftState.HEAR_BEAT, EventType.SUCCESS, (o, e ) -> {
            return RaftState.HEAR_BEAT;
        });

        // 心跳 -> 失败 = 监听心跳
        stateMachineFactory.addTransition(RaftState.HEAR_BEAT, RaftState.LISTENING_HEARTBEAT, EventType.FAIL, (o, e ) -> {
            return RaftState.LISTENING_HEARTBEAT;
        });

        // 加入初始状态
        stateMachine = stateMachineFactory.make(new Context(), this.raftState);
    }

}
