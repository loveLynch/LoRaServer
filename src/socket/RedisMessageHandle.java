package socket;

import redis.clients.jedis.Jedis;
import util.RedisDB;
import util.RedisPubSub;

/**
 * Created by lynch on 2018/6/4. <br>
 **/
public class RedisMessageHandle implements Runnable {
    @Override
    public void run() {
        RedisDB redisDB = new RedisDB();
        redisDB.setup();
        // redis Subscribe and Publish
        // Subscribe
        System.out.println("****Redis Subscribe Start****");
        Jedis jedis = redisDB.jedisPool.getResource();
        RedisPubSub listener = new RedisPubSub();
        jedis.subscribe(listener, "CommandChannel");
        //Publish
        jedis.publish("ACKChannel", "test");

    }

    //测试消息发布，信道为ACKChannel
    public static void main(String[] args) {
        RedisDB redisDB = new RedisDB();
        redisDB.setup();
        Jedis jedis = redisDB.jedisPool.getResource();
        //Publish
        while (true) {
            jedis.publish("ACKChannel", "test");
        }

    }
}
