package configs;

import java.util.ArrayList;
import java.util.List;
import graph.Message;

public class Node {
    private String name;
    private List<Node> edges;
    private Message message;

    public Node(String name) {
        this.name = name;
        this.edges = new ArrayList<>();
        this.message = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Node> getEdges() {
        return edges;
    }

    public void setEdges(List<Node> edges) {
        this.edges = edges;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    // Appends a unique directed edge pointing to a target neighbor node
    public void addEdge(Node node) {
        if (node != null && !edges.contains(node)) {
            edges.add(node);
        }
    }

    // Checks whether this node belongs to a recursive cyclical loop path
    public boolean hasCycles() {
        List<Node> visited = new ArrayList<>();
        List<Node> stack = new ArrayList<>();
        return hasCyclesDFS(this, visited, stack);
    }

    // Executes DFS cycle detection tracking the current recursive path stack
    private boolean hasCyclesDFS(Node current, List<Node> visited, List<Node> stack) {
        if (stack.contains(current)) {
            return true; 
        }
        if (visited.contains(current)) {
            return false; 
        }

        visited.add(current);
        stack.add(current);

        for (Node neighbor : current.getEdges()) {
            if (hasCyclesDFS(neighbor, visited, stack)) {
                return true;
            }
        }

        stack.remove(current); 
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Node node = (Node) obj;
        return name != null ? name.equals(node.name) : node.name == null;
    }
}