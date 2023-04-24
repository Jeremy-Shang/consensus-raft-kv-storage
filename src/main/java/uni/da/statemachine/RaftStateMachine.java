package uni.da.statemachine;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uni.da.common.Pipe;
import uni.da.statemachine.fsm.impl.StateMachineFactory;
import uni.da.statemachine.fsm.component.Context;
import uni.da.statemachine.fsm.component.Event;
import uni.da.statemachine.fsm.component.EventType;
import uni.da.statemachine.fsm.StateMachine;
import uni.da.statemachine.task.ElectionTask;
import uni.da.statemachine.task.HearBeatBroadcastTask;
import uni.da.statemachine.task.HeartBeatListenTask;
import uni.da.statemachine.fsm.component.State;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Data
public class RaftStateMachine implements Runnable {

    private Map<State, Callable<EventType>> taskMap = new ConcurrentHashMap<>();

    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private StateMachine<State, EventType, Event> stateMachine;

    private State state = State.LISTENING_HEARTBEAT;

    private Pipe heartBeatPipe = new Pipe("heartBeat");

    public RaftStateMachine() throws IOException {

        taskMap.put(State.LISTENING_HEARTBEAT, new HeartBeatListenTask(heartBeatPipe));
        taskMap.put(State.ELECTION, new ElectionTask());
        taskMap.put(State.HEAR_BEAT, new HearBeatBroadcastTask());

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
        Callable<EventType> currTask = taskMap.get(this.state);

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
                this.state = stateMachine.doTransition(futureEventType, new Event(futureEventType));
                currTask = taskMap.get(this.state);
            }
        }
    }


    /*
        状态机状态注册
     */
    private void stateTransferRegistry() {
        StateMachineFactory<Context, State, EventType, Event> stateMachineFactory = new StateMachineFactory<>();

        // 心跳监听 -> 成功 = 心跳监听
        stateMachineFactory.addTransition(State.LISTENING_HEARTBEAT, State.LISTENING_HEARTBEAT, EventType.SUCCESS, (o, e ) -> {
            return State.LISTENING_HEARTBEAT;
        });
        // 心跳监听 -> 失败 = 选举
        stateMachineFactory.addTransition(State.LISTENING_HEARTBEAT, State.ELECTION, EventType.FAIL, (o, e ) -> {
            return State.ELECTION;
        });
        // 选举 -> 成功 = 心跳
        stateMachineFactory.addTransition(State.ELECTION, State.HEAR_BEAT, EventType.SUCCESS, (o, e ) -> {
            return State.HEAR_BEAT;
        });
        // 选举 -> 失败 = 心跳监听
        stateMachineFactory.addTransition(State.ELECTION, State.LISTENING_HEARTBEAT, EventType.FAIL, (o, e ) -> {
            return State.LISTENING_HEARTBEAT;
        });

        // 心跳 -> 成功 = 心跳
        stateMachineFactory.addTransition(State.HEAR_BEAT, State.HEAR_BEAT, EventType.SUCCESS, (o, e ) -> {
            return State.HEAR_BEAT;
        });

        // 心跳 -> 失败 = 监听心跳
        stateMachineFactory.addTransition(State.HEAR_BEAT, State.LISTENING_HEARTBEAT, EventType.FAIL, (o, e ) -> {
            return State.LISTENING_HEARTBEAT;
        });

        // 加入初始状态
        stateMachine = stateMachineFactory.make(new Context(), this.state);
    }

}
