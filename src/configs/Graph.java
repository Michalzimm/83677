package configs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import graph.TopicManagerSingleton.TopicManager;
import graph.TopicManagerSingleton;
import graph.Topic;
import graph.Agent;

public class Graph extends ArrayList<Node> {
    
    // Checks if any node within the entire graph contains a back-edge loop
    public boolean hasCycles() {
        for (Node node : this) {
            if (node.hasCycles()) {
                return true; 
            }
        }
        return false; 
    }

    // Scans live communication topologies to reconstruct the visual data layout
    public void createFromTopics() {
        this.clear(); 
        
        // Trackers ensuring node uniqueness across both categories
        Map<String, Node> topicNodes = new HashMap<>();
        Map<String, Node> agentNodes = new HashMap<>();

        TopicManager tm = TopicManagerSingleton.get();

        for (Topic topic : tm.getTopics()) {
            String tName = "T" + topic.name;
            Node tNode = topicNodes.computeIfAbsent(tName, Node::new);

            // Connects a source topic to all downstream consumer agents
            for (Agent agent : topic.getSubs()) {
                String aName = "A" + agent.getName();
                Node aNode = agentNodes.computeIfAbsent(aName, Node::new);
                tNode.addEdge(aNode);
            }

            // Connects upstream publishing agents to their respective target topic
            for (Agent agent : topic.getPubs()) {
                String aName = "A" + agent.getName();
                Node aNode = agentNodes.computeIfAbsent(aName, Node::new);
                aNode.addEdge(tNode);
            }
        }

        this.addAll(topicNodes.values());
        this.addAll(agentNodes.values());
    }    
}