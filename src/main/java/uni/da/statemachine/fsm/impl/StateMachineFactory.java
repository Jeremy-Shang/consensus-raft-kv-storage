package uni.da.statemachine.fsm.impl;

import lombok.var;
import uni.da.statemachine.fsm.StateMachine;
import uni.da.statemachine.fsm.Transition;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * FSM 工厂
 *
 * @param <O> O operand 操作数类型
 * @param <S> S 状态机状态枚举类型
 * @param <T> T 事件类型
 * @param <E> E 事件
 * @author
 */
public final class StateMachineFactory<O, S extends Enum<S>, T extends Enum<T>, E> {

    /**
     * 通过当前状态和事件类型获取用户注册的四元组
     * 状态 State(S) + Event(E, EventType(T)) + Operator(O)  -> State(S)'
     */
    private final Map<S, Map<T, Transition<O, E, S>>> transitionMachineTable = new HashMap<>();

    public StateMachineFactory<O, S, T, E> addTransition(S preState, S postState, T eventType,
                                                         Transition<O, E, S> hook) {
        var transitionMap = transitionMachineTable.computeIfAbsent(preState, k -> new HashMap<>(8));
        transitionMap.put(eventType, (o, e) -> {
            S r = hook.transition(o, e);
            if (Objects.equals(r, postState)) {
                return postState;
            }
            throw new RuntimeException("Invalid event: " + e + " at " + preState);
        });
        return this;
    }

    public StateMachineFactory<O, S, T, E> addTransition(S preState, Set<S> postStates, T eventType,
                                                         Transition<O, E, S> hook) {
        var transitionMap = transitionMachineTable.computeIfAbsent(preState, k -> new HashMap<>(8));
        transitionMap.put(eventType, (o, e) -> {
            S r = hook.transition(o, e);
            if (postStates.contains(r)) {
                return r;
            }
            throw new RuntimeException("Invalid event: " + e + " at " + preState);
        });
        return this;
    }

    public StateMachine<S, T, E> make(O operand, S initialState) {
        return new InternalStateMachine(operand, initialState);
    }

    /**
     * 真正的状态机，一个状态机维护一个对象 operand 和该对象的状态
     * 状态转换规则使用 {@link StateMachineFactory} 中的 {@link #transitionMachineTable}
     */
    private class InternalStateMachine
            implements StateMachine<S, T, E> {
        private final O operand;
        private S currentState;

        InternalStateMachine(O operand, S initialState) {
            this.operand = operand;
            this.currentState = initialState;
        }

        @Override
        public synchronized S getCurrentState() {
            return currentState;
        }

        @Override
        public synchronized S doTransition(T eventType, E event) {
            var transitionMap = transitionMachineTable.get(currentState);
            if (transitionMap != null) {
                var transition = transitionMap.get(eventType);
                if (transition != null) {
                    currentState = transition.transition(operand, event);
                    return currentState;
                }
            }
            throw new RuntimeException("Invalid event: " + event + " at " + currentState);
        }
    }
}