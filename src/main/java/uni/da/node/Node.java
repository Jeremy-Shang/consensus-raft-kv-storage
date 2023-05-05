package uni.da.node;


/*
    Raft 集群节点
 */
public interface Node extends RaftModule{

    void put(Object key, Object value);

    void get(Object key);
}
