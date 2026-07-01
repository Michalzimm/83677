package graph;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Topic {
    public final String name;
    // CopyOnWriteArrayList prevents ConcurrentModificationException during concurrent modifications
    private final List<Agent> subs = new CopyOnWriteArrayList<>();
    private final List<Agent> pubs = new CopyOnWriteArrayList<>();

    Topic(String name) {
        this.name = name;
    }

    // Registers an agent as a subscriber to this topic
    public void subscribe(Agent a) {
        if (a != null && !subs.contains(a)) {
            subs.add(a);
        }
    }

    // Removes an agent from the subscriber list
    public void unsubscribe(Agent a) {
        subs.remove(a);
    }

    // Distributes a message to all currently subscribed agents
    public void publish(Message m) {
        for (Agent agent : subs) {
            agent.callback(this.name, m);
        }
    }

    // Registers an agent as a publisher for this topic
    public void addPublisher(Agent a) {
        if (a != null && !pubs.contains(a)) {
            pubs.add(a);
        }
    }

    // Removes an agent from the publisher list
    public void removePublisher(Agent a) {
        pubs.remove(a);
    }
    
    // Returns the list of subscribed agents
    public List<Agent> getSubs() {
        return subs;
    }

    // Returns the list of registered publishers
    public List<Agent> getPubs() {
        return pubs;
    }
}