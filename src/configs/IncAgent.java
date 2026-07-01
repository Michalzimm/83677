package configs;

import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;
import graph.Agent;
import graph.Message;

public class IncAgent implements Agent {
    private final String name;
    private final String[] subs;
    private final String[] pubs;

    // Sets up streaming subscriptions for a single input/output increment setup
    public IncAgent(String[] subs, String[] pubs) {
        this.subs = subs;
        this.pubs = pubs;
        this.name = "IncAgent";

        TopicManager tm = TopicManagerSingleton.get();
        if (subs.length >= 1) {
            tm.getTopic(subs[0]).subscribe(this);
        }
        if (pubs.length >= 1) {
            tm.getTopic(pubs[0]).addPublisher(this);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void reset() {
        // No internal state to clear for this agent
    }

    // Increments the incoming message value by 1 and publishes the result
    @Override
    public void callback(String topic, Message msg) {
        if (subs.length >= 1 && topic.equals(subs[0])) {
            double val = msg.asDouble;
            if (!Double.isNaN(val) && pubs.length >= 1) {
                double result = val + 1;
                TopicManagerSingleton.get().getTopic(pubs[0]).publish(new Message(result));
            }
        }
    }

    // Disconnects subscriptions and publisher configurations
    @Override
    public void close() {
        TopicManager tm = TopicManagerSingleton.get();
        if (subs.length >= 1) {
            tm.getTopic(subs[0]).unsubscribe(this);
        }
        if (pubs.length >= 1) {
            tm.getTopic(pubs[0]).removePublisher(this);
        }
    }
}