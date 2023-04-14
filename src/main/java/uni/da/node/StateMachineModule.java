package uni.da.node;

/*
    Raft 单节点内的状态机
 */
public interface StateMachineModule extends RaftModule{

    void commit();

}
