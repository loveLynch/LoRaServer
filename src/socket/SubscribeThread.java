package socket;

import util.MessageQueueProduct;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import util.RedisDB;

/**
 * Created by xiaokaixiang on 2017/12/17.
 */
public class SubscribeThread implements Runnable{
    private JedisPubSub jedisPubSub;
    private Jedis jedis;
    private String channel;

    public SubscribeThread(String channel) {
        this.channel = channel;
        this.jedisPubSub = new MessageQueueProduct();
        this.jedis = RedisDB.jedisPool.getResource();
    }

    @Override
    public void run() {
        System.out.println("******************** SUBSCRIBE ********************");
        System.out.println("                  SUBSCRIBE START");
        System.out.println("***************************************************");

        jedis.subscribe(jedisPubSub, channel);

    }
}
