package demo.redis_vs_beanstalkd;

import com.surftools.BeanstalkClientImpl.ClientImpl;

public class BeanstalkProducer implements Runnable {

    private static final String QUEUE_NAME = "message_queue";
    private static final int MESSAGES_PER_THREAD = 100_000; // Messages each thread will produce
    private static final String MESSAGE = "Heavy load message";
    private static final int THREAD_COUNT = 10; // Number of threads

    public static void main(String[] args) throws InterruptedException {
        var producers = new Thread[THREAD_COUNT];
        var startTime = System.currentTimeMillis();

        for (var i = 0; i < THREAD_COUNT; i++) {
            producers[i] = new Thread(new BeanstalkProducer());
            producers[i].start();
        }

        for (var producer : producers) {
            producer.join();
        }

        var endTime = System.currentTimeMillis();
        System.out.println("Produced " + (MESSAGES_PER_THREAD * THREAD_COUNT) + " messages in " + (endTime - startTime) + " ms");
    }

    @Override
    public void run() {
        var client = new ClientImpl("localhost", 11300);
        client.useTube(QUEUE_NAME);

        for (var i = 0; i < MESSAGES_PER_THREAD; i++) {
            var message = MESSAGE + i + " from thread " + Thread.currentThread().getId();
            client.put(1, 0, 120, message.getBytes());
            System.out.println("Produced message " + i);
        }

        client.close();
    }
}
