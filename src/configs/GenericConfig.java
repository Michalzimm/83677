package configs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import graph.Agent;
import graph.ParallelAgent;

public class GenericConfig implements Config {
    private String fileName;
    private final List<ParallelAgent> activeAgents;
    // Holds the dynamic structural graph for view rendering
    private final Graph graph; 

    public GenericConfig() {
        this.activeAgents = new ArrayList<>();
        this.graph = new Graph();
    }

    public void setConfFile(String fileName) {
        this.fileName = fileName;
    }

    // Allows servlets to extract the runtime graph blueprint
    public Graph getGraph() {
        return this.graph;
    }

    @Override
    public void create() {
        List<String> lines = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (lines.size() % 3 != 0) {
            return;
        }

        for (int i = 0; i < lines.size(); i += 3) {
            String fullClassName = lines.get(i);
            String subsLine = lines.get(i + 1);
            String pubsLine = lines.get(i + 2);

            String[] subs = subsLine.isEmpty() ? new String[0] : subsLine.split("\\s*,\\s*");
            String[] pubs = pubsLine.isEmpty() ? new String[0] : pubsLine.split("\\s*,\\s*");

            try {
                Class<?> clazz = null;
                try {
                    clazz = Class.forName(fullClassName);
                } catch (ClassNotFoundException e) {
                    String className = fullClassName;
                    if (fullClassName.contains(".")) {
                        className = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
                    }
                    try {
                        clazz = Class.forName("test." + className);
                    } catch (ClassNotFoundException ex) {
                        try {
                            clazz = Class.forName("Config." + className);
                        } catch (ClassNotFoundException exc) {
                            clazz = Class.forName("configs." + className);
                        }
                    }
                }

                Constructor<?> constructor = clazz.getConstructor(String[].class, String[].class);
                Agent underlyingAgent = (Agent) constructor.newInstance((Object) subs, (Object) pubs);
                ParallelAgent parallelAgent = new ParallelAgent(underlyingAgent, 10);
                activeAgents.add(parallelAgent);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Rebuilds the graph layout structure based on live messaging channels
        this.graph.createFromTopics();
    }

    @Override
    public String getName() {
        return "Generic Config";
    }

    @Override
    public int getVersion() {
        return 1;
    }

    // Terminates all threads gracefully and empties the registry
    @Override
    public void close() {
        for (ParallelAgent pAgent : activeAgents) {
            if (pAgent != null) {
                pAgent.close();
            }
        }
        activeAgents.clear();
    }
}