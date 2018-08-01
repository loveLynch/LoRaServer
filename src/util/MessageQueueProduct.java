package util;


import main.LoRaMain;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.Date;

public class MessageQueueProduct extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        System.out.println("******************** SUBSCRIBE ********************");
        System.out.println("time:" + new Date().toString() + " channel:" + channel + " message:" + message);
        System.out.println("***************************************************");
        LoRaMain.OrderQueue.offer(message);
        RedisDB.jedisPool.getResource().publish("OrderACK", message + "_ACK");
//		Redis.jedisPool.getResource().lpush("doingQueue", message);
//		MessageHandle.handle(message);
    }

    @Override
    public void onPMessage(String arg0, String arg1, String arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPSubscribe(String arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPUnsubscribe(String arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSubscribe(String arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUnsubscribe(String arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    public static void main(String[] args) {
        MessageQueueProduct messageQueueProduct = new MessageQueueProduct();
        RedisDB redisDB = new RedisDB();
        redisDB.setup();
        Jedis jedis = RedisDB.jedisPool.getResource();
        jedis.subscribe(messageQueueProduct, "xkx");
    }

}
