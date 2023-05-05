package uni.da.statetransfer.fsm;


/**
 * A general statemachine for state transfer. Using existing code from website https://www.rectcircle.cn/posts/java-state-machine-impl/
 * and related GitHub repo.
 * @param <S>
 * @param <T>
 * @param <E>
 */
public interface StateMachine<S extends Enum<S>, T extends Enum<T>, E> {


    S getCurrentState();


    S doTransition(T eventType, E event);
}