package uni.da.node;


/*
    Raft 集群节点
 */
public interface Node extends RaftModule{

    // Client 功能
    void put(Object key, Object value);

    void get(Object key);
}
