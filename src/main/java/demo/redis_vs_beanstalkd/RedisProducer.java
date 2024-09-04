package demo.redis_vs_beanstalkd;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;

public class RedisProducer implements Runnable {

    private static final String QUEUE_NAME = "message_queue";
    private static final int MESSAGES_PER_THREAD = 100_000;
    private static final String MESSAGE = "Heavy load message";
    private static final int THREAD_COUNT = 10;

    public static void main(String[] args) throws InterruptedException {
        var producers = new Thread[THREAD_COUNT];
        var startTime = System.currentTimeMillis();

        // Start producer threads
        for (var i = 0; i < THREAD_COUNT; i++) {
            producers[i] = new Thread(new RedisProducer());
            producers[i].start();
        }

        // Wait for all producers to finish
        for (var producer : producers) {
            producer.join();
        }

        var endTime = System.currentTimeMillis();
        System.out.println("Produced " + (MESSAGES_PER_THREAD * THREAD_COUNT) + " messages in " + (endTime - startTime) + " ms");
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
            jedis.rpush(QUEUE_NAME, MESSAGE + i + " from thread " + Thread.currentThread().getId());
            System.out.println("Produced message " + i);
        }

        jedis.close();
    }
}

