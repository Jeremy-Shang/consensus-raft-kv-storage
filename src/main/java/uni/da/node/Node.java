package uni.da.node;


/*
    Raft 集群节点
 */
public interface Node extends RaftModule{
    // 启动

    // 召集一次选举
    void election();

    // 心跳
    void heartBeat();

    // Client 功能
    void put(Object key, Object value);

    void get(Object key);

}
