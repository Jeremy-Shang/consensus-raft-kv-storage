package uni.da.node;

import uni.da.entity.Log.LogBody;
import uni.da.entity.Log.LogEntry;

public interface StateMachineModule extends RaftModule{

    public void commit(LogBody body);

    public String get(int key);

}
