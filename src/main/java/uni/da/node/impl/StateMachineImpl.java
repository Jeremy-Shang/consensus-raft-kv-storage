package uni.da.node.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import uni.da.common.RedisDb;
import uni.da.entity.Log.LogBody;
import uni.da.entity.Log.LogEntry;
import uni.da.node.RaftModule;
import uni.da.node.StateMachineModule;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class StateMachineImpl implements StateMachineModule {

    @NonNull String nodeId;

    private ConcurrentHashMap<Integer, String> stateMachine = new ConcurrentHashMap<>();

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

        RedisDb.setJsonString(nodeId + "-statemachine", stateMachine);
    }
}
