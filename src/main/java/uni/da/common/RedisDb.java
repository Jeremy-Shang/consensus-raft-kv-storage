package uni.da.common;

import com.alibaba.fastjson.JSON;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Redis Db for persistence storage
 *
 * save: client's command (apply state machine), log module
 */
public class RedisDb {
    private static JedisPool jedisPool = null;

    public static void set(String key,String value){
        Jedis jedis = RedisDb.getJedis();
        jedis.set(key, value);
        jedis.close();
    }
    public static String get(String key){
        Jedis jedis = RedisDb.getJedis();
        String value = jedis.get(key);
        jedis.close();
        return value;
    }
    public static void setJsonString(String key,Object object){
        Jedis jedis = RedisDb.getJedis();
        jedis.set(key, JSON.toJSONString(object));
        jedis.close();
    }
    public static Object getJsonObject(String key,Class clazz){
        Jedis jedis = RedisDb.getJedis();
        String value = jedis.get(key);
        jedis.close();
        return JSON.parseObject(value,clazz);
    }

    public synchronized static Jedis getJedis() {
        try {
            if(jedisPool == null){
                jedisPool = new JedisPool();
            }
            Jedis jedis = jedisPool.getResource();
            return jedis;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
