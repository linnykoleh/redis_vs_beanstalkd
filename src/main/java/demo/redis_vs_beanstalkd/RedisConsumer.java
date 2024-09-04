package demo.redis_vs_beanstalkd;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;

public class RedisConsumer implements Runnable {

    private static final String QUEUE_NAME = "message_queue";
    private static final int MESSAGES_PER_THREAD = 100_000; // Messages each thread will consume
    private static final int THREAD_COUNT = 10;

    public static void main(String[] args) throws InterruptedException {
        var consumers = new Thread[THREAD_COUNT];
        var startTime = System.currentTimeMillis();

        // Start consumer threads
        for (var i = 0; i < THREAD_COUNT; i++) {
            consumers[i] = new Thread(new RedisConsumer());
            consumers[i].start();
        }

        // Wait for all consumers to finish
        for (var consumer : consumers) {
            consumer.join();
        }

        var endTime = System.currentTimeMillis();
        System.out.println("Consumed " + (MESSAGES_PER_THREAD * THREAD_COUNT) + " messages in " + (endTime - startTime) + " ms");
    }

    @Override
    public void run() {
        var jedis = new Jedis("localhost", 6379, new JedisClientConfig() {
            @Override
            public String getPassword() {
                return "yourmasterpassword";
            }
        });

        for (var i = 0; i < MESSAGES_PER_THREAD; i++) {
            var message = jedis.blpop(0, QUEUE_NAME);// Blocking pop
            System.out.println("Consumed message: " + message);
        }

        jedis.close();
    }
}

