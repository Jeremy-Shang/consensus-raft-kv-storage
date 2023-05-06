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
import uni.da.util.LogType;

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
            log.debug("[{}] current state: {}, curr task: {}, curr term: {}" , LogType.STATE_TRANSFER, stateMachine.getCurrentState().toString(), currTask.getClass().getName(), consensusState.getCurrTerm());

            Future<EventType> future = consensusState.getNodeExecutorService().submit(currTask);
            EventType futureEventType = EventType.FAIL;

            try {
                futureEventType = future.get(consensusState.getTimeout(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                log.error("======> task {} timeout at: {}ms term {}", currTask.getClass().getName(), consensusState.getTimeout(), consensusState.getCurrTerm());
                futureEventType = EventType.TIME_OUT;
                e.printStackTrace();
            } catch (InterruptedException e) {
                log.error("======> task {} interrupted term{}", currTask.getClass().getName(), consensusState.getCurrTerm());
                futureEventType = EventType.FAIL;
                e.printStackTrace();
            } catch (ExecutionException e) {
                log.error("======> task {} execution fail term{}", currTask.getClass().getName(), consensusState.getCurrTerm());

                futureEventType = EventType.FAIL;
                e.printStackTrace();
            } finally {
                this.raftState = stateMachine.doTransition(futureEventType, new Event(futureEventType));
                currTask = taskMap.get(this.raftState);
            }
        }
    }



    private void stateTransferRegistry() {

        StateMachineFactory<Context, RaftState, EventType, Event> stateMachineFactory = new StateMachineFactory<>();

        stateMachineFactory.addTransition(RaftState.LISTENING_HEARTBEAT, RaftState.LISTENING_HEARTBEAT, EventType.SUCCESS, (o, e ) -> {
            return RaftState.LISTENING_HEARTBEAT;
        });

        stateMachineFactory.addTransition(RaftState.LISTENING_HEARTBEAT, RaftState.ELECTION, EventType.FAIL, (o, e ) -> {
            return RaftState.ELECTION;
        });

        stateMachineFactory.addTransition(RaftState.LISTENING_HEARTBEAT, RaftState.ELECTION, EventType.TIME_OUT, (o, e ) -> {
            return RaftState.ELECTION;
        });


        stateMachineFactory.addTransition(RaftState.ELECTION, RaftState.HEAR_BEAT, EventType.SUCCESS, (o, e ) -> {
            return RaftState.HEAR_BEAT;
        });

        stateMachineFactory.addTransition(RaftState.ELECTION, RaftState.LISTENING_HEARTBEAT, EventType.FAIL, (o, e ) -> {
            return RaftState.LISTENING_HEARTBEAT;
        });

        stateMachineFactory.addTransition(RaftState.ELECTION, RaftState.ELECTION, EventType.TIME_OUT, (o, e ) -> {
            return RaftState.ELECTION;
        });


        stateMachineFactory.addTransition(RaftState.HEAR_BEAT, RaftState.HEAR_BEAT, EventType.SUCCESS, (o, e ) -> {
            return RaftState.HEAR_BEAT;
        });

        stateMachineFactory.addTransition(RaftState.HEAR_BEAT, RaftState.HEAR_BEAT, EventType.FAIL, (o, e ) -> {
            return RaftState.HEAR_BEAT;
        });

        stateMachineFactory.addTransition(RaftState.HEAR_BEAT, RaftState.HEAR_BEAT, EventType.TIME_OUT, (o, e ) -> {
            return RaftState.HEAR_BEAT;
        });


        stateMachine = stateMachineFactory.make(new Context(), this.raftState);
    }

}
