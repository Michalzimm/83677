package graph;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ParallelAgent implements Agent {

    // Data container pairing a topic name with its published message
    private static class TopicMessagePair {
        final String topic;
        final Message message;

        TopicMessagePair(String topic, Message message) {
            this.topic = topic;
            this.message = message;
        }
    }

    private final Agent agent;
    private final BlockingQueue<TopicMessagePair> queue;
    private final Thread workerThread;
    private volatile boolean isRunning;

    // Wraps a sequential agent and instantiates a background worker thread
    public ParallelAgent(Agent agent, int capacity) {
        this.agent = agent;
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.isRunning = true;

        // Background worker loop executing callbacks asynchronously
        this.workerThread = new Thread(() -> {
            while (isRunning || !queue.isEmpty()) {
                try {
                    TopicMessagePair pair = queue.take();
                    if (pair.message != null) {
                        this.agent.callback(pair.topic, pair.message);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); 
                }
            }
        });
        
        this.workerThread.start();
    }

    @Override
    public String getName() {
        return this.agent.getName();
    }

    @Override
    public void reset() {
        this.agent.reset();
    }

    // Enqueues incoming message processing tasks without blocking
    @Override
    public void callback(String topic, Message msg) {
        if (!isRunning) return;
        
        try {
            queue.put(new TopicMessagePair(topic, msg));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Signals the background thread to terminate and waits cleanly to finish
    @Override
    public void close() {
        this.isRunning = false;
        this.workerThread.interrupt();
        
        try {
            this.workerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        this.agent.close();
    }
}