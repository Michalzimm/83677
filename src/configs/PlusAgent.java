package configs;

import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;
import graph.Agent;
import graph.Message;

public class PlusAgent implements Agent {
    private final String name;
    private final String[] subs;
    private final String[] pubs;
    private double x = 0.0; 
    private double y = 0.0; 

    // Binds a custom addition agent to two input topics and one target publisher output
    public PlusAgent(String[] subs, String[] pubs) {
        this.subs = subs;
        this.pubs = pubs;
        this.name = "PlusAgent";

        TopicManager tm = TopicManagerSingleton.get();
        if (subs.length >= 2) {
            tm.getTopic(subs[0]).subscribe(this);
            tm.getTopic(subs[1]).subscribe(this);
        }
        if (pubs.length >= 1) {
            tm.getTopic(pubs[0]).addPublisher(this);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    // Clears internal addition operands back to default 0.0
    @Override
    public void reset() {
        this.x = 0.0;
        this.y = 0.0;
    }

    // Captures channel updates and pushes the sum of both inputs down the pipeline
    @Override
    public void callback(String topic, Message msg) {
        if (subs.length >= 2) {
            if (topic.equals(subs[0])) {
                x = msg.asDouble;
            } else if (topic.equals(subs[1])) {
                y = msg.asDouble;
            }
        }

        if (!Double.isNaN(x) && !Double.isNaN(y) && pubs.length >= 1) {
            double result = x + y;
            TopicManagerSingleton.get().getTopic(pubs[0]).publish(new Message(result));
        }
    }

    // Cleans up all registered pipeline handles cleanly
    @Override
    public void close() {
        TopicManager tm = TopicManagerSingleton.get();
        if (subs.length >= 2) {
            tm.getTopic(subs[0]).unsubscribe(this);
            tm.getTopic(subs[1]).unsubscribe(this);
        }
        if (pubs.length >= 1) {
            tm.getTopic(pubs[0]).removePublisher(this);
        }
    }
}