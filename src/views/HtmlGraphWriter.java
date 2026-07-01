package views;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import configs.Graph;
import configs.Node;

public class HtmlGraphWriter {

    
     //Reads the static HTML template and populates it dynamically with nodes and edges.
    public static String getGraphHTML(Graph graph) {
        String templatePath = "html_files/graph.html";
        String htmlContent = "";

        try {
            htmlContent = new String(Files.readAllBytes(Paths.get(templatePath)));
        } catch (IOException e) {
            e.printStackTrace();
            return "<h3>Error: Failed to load graph.html template.</h3>";
        }

        StringBuilder nodesBuilder = new StringBuilder();
        StringBuilder edgesBuilder = new StringBuilder();

        int nodeCounter = 0;
        int edgeCounter = 0;

        for (Node node : graph) {
            if (nodeCounter > 0) {
                nodesBuilder.append(",\n");
            }

            String fullId = node.getName();
            String cleanName = fullId;

            // Strip prefix if present
            if (fullId.startsWith("T_") || fullId.startsWith("A_")) {
                cleanName = fullId.substring(2);
            }

            // Determine if the node is a Topic or an Agent
            if (fullId.startsWith("T_") || fullId.startsWith("T") || !fullId.contains("Agent")) {
                String lastVal = "NaN";
                if (node.getMessage() != null) {
                    lastVal = String.valueOf(node.getMessage().asDouble);
                }

                // Append Topic (Green box)
                nodesBuilder.append("{ id: '").append(fullId).append("', ")
                            .append("label: 'Topic: ").append(cleanName).append("\\n(").append(lastVal).append(")', ")
                            .append("shape: 'box', color: { background: '#d4edda', border: '#28a745' } }");
            } else {
                // Shorten class path name for display if necessary
                if (cleanName.contains(".")) {
                    cleanName = cleanName.substring(cleanName.lastIndexOf('.') + 1);
                }

                // Append Agent (Blue circle)
                nodesBuilder.append("{ id: '").append(fullId).append("', ")
                            .append("label: '").append(cleanName).append("', ")
                            .append("shape: 'circle', color: { background: '#cce5ff', border: '#007bff' } }");
            }
            nodeCounter++;

            // Build directed edges
            if (node.getEdges() != null) {
                for (Node neighbor : node.getEdges()) {
                    if (edgeCounter > 0) {
                        edgesBuilder.append(",\n");
                    }
                    edgesBuilder.append("{ from: '").append(fullId).append("', to: '").append(neighbor.getName()).append("' }");
                    edgeCounter++;
                }
            }
        }

        if (nodesBuilder.length() == 0) {
            nodesBuilder.append("{ id: 'empty', label: 'No Active Nodes Loaded', shape: 'box' }");
        }

        // Inject generated structure data into placeholders
        htmlContent = htmlContent.replace("__NODES_DATA__", nodesBuilder.toString());
        htmlContent = htmlContent.replace("__EDGES_DATA__", edgesBuilder.toString());

        return htmlContent;
    }
}