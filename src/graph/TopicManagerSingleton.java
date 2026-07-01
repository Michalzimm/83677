package graph;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class TopicManagerSingleton {

    public static class TopicManager {
        
        // Static instance accessible instantly for internal and reflection-based tests
        public static final TopicManager instance = new TopicManager();
        
        // ConcurrentHashMap provides thread-safe topic registry storage
        private final ConcurrentHashMap<String, Topic> topics = new ConcurrentHashMap<>();

        private TopicManager() {}

        // Retrieves an existing topic or creates a new one if missing
        public Topic getTopic(String name) {
            return topics.computeIfAbsent(name, Topic::new);
        }

        // Returns all registered topics in the system
        public Collection<Topic> getTopics() {
            return topics.values();
        }

        // Clears all registered topics from the memory store
        public void clear() {
            topics.clear();
        }
    }

    // Global access point to retrieve the inner TopicManager singleton instance
    public static TopicManager get() {
        return TopicManager.instance;
    }
}