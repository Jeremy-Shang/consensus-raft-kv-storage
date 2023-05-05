package uni.da.statetransfer.fsm;

/**
 * FSM 状态机接口
 *
 * @param <S> 状态枚举类型
 * @param <T> 事件类型
 * @param <E> 事件
 * @author
 */
public interface StateMachine<S extends Enum<S>, T extends Enum<T>, E> {

    /**
     * 获取状态机的当前状态
     * @return
     */
    S getCurrentState();

    /**
     * 做一次状态转换
     * @param eventType
     * @param event
     * @return
     */
    S doTransition(T eventType, E event);
}