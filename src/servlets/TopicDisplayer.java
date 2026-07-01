package servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import servlets.Servlet;
import server.RequestParser.RequestInfo;
import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;

public class TopicDisplayer implements Servlet {

    // Tracks final runtime parameter data states cleanly across channel operations
    private static final ConcurrentHashMap<String, String> lastValuesMap = new ConcurrentHashMap<>();

    // Processes parameter tokens and publishes messaging values downstream
    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        String topicName = ri.getParameters().get("topic");
        String messageStr = ri.getParameters().get("message");

        if (topicName != null && messageStr != null) {
            try {
                double val = Double.parseDouble(messageStr);
                Topic topic = TopicManagerSingleton.get().getTopic(topicName);
                if (topic != null) {
                    topic.publish(new Message(val)); 
                    lastValuesMap.put(topicName, String.valueOf(val));
                }
            } catch (NumberFormatException e) {
                // Ignore invalid number formats safely
            }
        }

        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html>\n")
                   .append("<html>\n")
                   .append("<head>\n")
                   .append("<style>\n")
                   .append("body { font-family: Arial, sans-serif; padding: 10px; background-color: #ffffff; }\n")
                   .append("table { width: 100%; border-collapse: collapse; margin-top: 10px; }\n")
                   .append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n")
                   .append("th { background-color: #007bff; color: white; }\n")
                   .append("tr:nth-child(even) { background-color: #f9f9f9; }\n")
                   .append("</style>\n")
                   .append("</head>\n")
                   .append("<body>\n")
                   .append("<h3>Active Topics Status</h3>\n")
                   .append("<table>\n")
                   .append("<tr><th>Topic Name</th><th>Last Value</th></tr>\n"); 

        Collection<Topic> topics = TopicManagerSingleton.get().getTopics();
        if (topics != null) {
            for (Topic t : topics) {
                String name = t.name; 
                String lastValue = lastValuesMap.getOrDefault(name, "No Message");
                
                htmlBuilder.append("<tr>")
                           .append("<td>").append(name).append("</td>")
                           .append("<td>").append(lastValue).append("</td>")
                           .append("</tr>\n");
            }
        }

        htmlBuilder.append("</table>\n")
                   .append("</body>\n")
                   .append("</html>");

        byte[] responseBytes = htmlBuilder.toString().getBytes();

        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                              "Content-Type: text/html\r\n" +
                              "Content-Length: " + responseBytes.length + "\r\n" +
                              "\r\n";
        
        toClient.write(httpResponse.getBytes());
        toClient.write(responseBytes);
        toClient.flush();
    }

    @Override
    public void close() throws IOException {
    }
}