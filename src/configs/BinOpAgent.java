package configs;

import java.util.function.BinaryOperator;
import graph.TopicManagerSingleton.TopicManager;
import graph.TopicManagerSingleton;
import graph.Agent;
import graph.Message;

public class BinOpAgent implements Agent {
    private final String name;
    private final String inputTopic1;
    private final String inputTopic2;
    private final String outputTopic;
    private final BinaryOperator<Double> operator;
    
    private Double val1 = 0.0;
    private Double val2 = 0.0;

    // Initializes fields and registers subscriptions via TopicManager
    public BinOpAgent(String name, String inputTopic1, String inputTopic2, String outputTopic, BinaryOperator<Double> operator) {
        this.name = name;
        this.inputTopic1 = inputTopic1;
        this.inputTopic2 = inputTopic2;
        this.outputTopic = outputTopic;
        this.operator = operator;

        TopicManager tm = TopicManagerSingleton.get();
        tm.getTopic(inputTopic1).subscribe(this);
        tm.getTopic(inputTopic2).subscribe(this);
        tm.getTopic(outputTopic).addPublisher(this);
    }

    @Override
    public String getName() {
        return this.name;
    }

    // Resets both cached input values back to 0.0
    @Override
    public void reset() {
        this.val1 = 0.0;
        this.val2 = 0.0;
    }

    // Updates cached values and triggers binary calculation upon receiving messages
    @Override
    public void callback(String topic, Message msg) {
        if (topic.equals(inputTopic1)) {
            val1 = msg.asDouble;
        } else if (topic.equals(inputTopic2)) {
            val2 = msg.asDouble;
        }

        if (val1 != null && val2 != null && !Double.isNaN(val1) && !Double.isNaN(val2)) {
            double result = operator.apply(val1, val2);
            TopicManagerSingleton.get().getTopic(outputTopic).publish(new Message(result));
        }
    }

    // Unregisters all active subscriber and publisher handles
    @Override
    public void close() {
        TopicManager tm = TopicManagerSingleton.get();
        tm.getTopic(inputTopic1).unsubscribe(this);
        tm.getTopic(inputTopic2).unsubscribe(this);
        tm.getTopic(outputTopic).removePublisher(this);
    }
}