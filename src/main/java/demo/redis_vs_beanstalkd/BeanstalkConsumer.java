package demo.redis_vs_beanstalkd;

import com.surftools.BeanstalkClientImpl.ClientImpl;

public class BeanstalkConsumer implements Runnable {

    private static final String QUEUE_NAME = "message_queue";
    private static final int MESSAGES_PER_THREAD = 100_000;
    private static final int THREAD_COUNT = 10;

    public static void main(String[] args) throws InterruptedException {
        var consumers = new Thread[THREAD_COUNT];
        var startTime = System.currentTimeMillis();

        for (var i = 0; i < THREAD_COUNT; i++) {
            consumers[i] = new Thread(new BeanstalkConsumer());
            consumers[i].start();
        }

        for (var consumer : consumers) {
            consumer.join();
        }

        var endTime = System.currentTimeMillis();
        System.out.println("Consumed " + (MESSAGES_PER_THREAD * THREAD_COUNT) + " messages in " + (endTime - startTime) + " ms");
    }

    @Override
    public void run() {
        var client = new ClientImpl("localhost", 11300);
        client.watch(QUEUE_NAME);  // Watch the tube for jobs

        for (var i = 0; i < MESSAGES_PER_THREAD; i++) {
            var job = client.reserve(10);

            if (job != null) {
                System.out.println("Consumed: " + new String(job.getData()));
                client.delete(job.getJobId());
            }
        }
        client.close();
    }
}