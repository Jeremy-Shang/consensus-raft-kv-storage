package uni.da.node.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import uni.da.common.RedisDb;
import uni.da.entity.Log.LogBody;
import uni.da.entity.Log.LogEntry;
import uni.da.node.StateMachineModule;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
public class StateMachineImpl implements StateMachineModule {

    String nodeId;

    String name;

    private ConcurrentHashMap<Integer, String> stateMachine = new ConcurrentHashMap<>();


    public StateMachineImpl(String id) {
        this.nodeId = id;
        this.name = id + "-statemachine";

        Jedis jedis = RedisDb.getJedis();

        if (jedis != null && jedis.exists(name)) {
            stateMachine = (ConcurrentHashMap<Integer, String>) RedisDb.getJsonObject(name, "stateMachine");
        } else {
            stateMachine = new ConcurrentHashMap<>();
        }
    }


    @Override
    public void start() throws InterruptedException, IOException {

    }

    @Override
    public void stop() {

    }

    /**
     * Save to persistence storage for recover
     * @param body
     */
    @Override
    public synchronized void commit(LogBody body) {

        stateMachine.put(body.getKey(), body.getValue());

        RedisDb.setJsonString(name, stateMachine);
    }

    @Override
    public String get(int key) {

        return stateMachine.get(key);
    }
}
