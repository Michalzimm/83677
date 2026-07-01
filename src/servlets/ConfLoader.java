package servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import server.RequestParser.RequestInfo;
import configs.GenericConfig;
import configs.Graph;

public class ConfLoader implements Servlet {

    private static final String CONFIG_SAVE_PATH = "html_files/uploaded_config.conf";

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        byte[] fileBytes = ri.getContent();
        
        if (fileBytes == null || fileBytes.length == 0) {
            System.out.println("[ConfLoader] Error: No bytes received from client.");
            sendErrorResponse(toClient, 400, "Bad Request - No file content received");
            return;
        }

        String fileContent = new String(fileBytes);
        String cleanConfigData = extractCleanConfig(fileContent);

        System.out.println("[ConfLoader] Cleaned config data length: " + cleanConfigData.length());
        System.out.println("[ConfLoader] Content preview:\n" + cleanConfigData);

        if (cleanConfigData.isEmpty()) {
            System.out.println("[ConfLoader] Error: Extracted config data is empty!");
            sendErrorResponse(toClient, 400, "Bad Request - Empty configuration after parsing");
            return;
        }

        Path path = Paths.get(CONFIG_SAVE_PATH);
        Files.write(path, cleanConfigData.getBytes());

        Graph runtimeGraph = null;

        try {
            // Instantiates configuration file loader and parses file
            GenericConfig config = new GenericConfig();
            config.setConfFile(CONFIG_SAVE_PATH);
            config.create(); 
            
            runtimeGraph = config.getGraph(); 
            
            String graphHtmlResponse = views.HtmlGraphWriter.getGraphHTML(runtimeGraph);
            System.out.println("[ConfLoader] Generated HTML length: " + graphHtmlResponse.length());
            
            byte[] responseBytes = graphHtmlResponse.getBytes();

            String httpResponse = "HTTP/1.1 200 OK\r\n" +
                                  "Content-Type: text/html; charset=utf-8\r\n" +
                                  "Content-Length: " + responseBytes.length + "\r\n" +
                                  "Connection: close\r\n" + 
                                  "\r\n";
            
            toClient.write(httpResponse.getBytes());
            toClient.write(responseBytes);
            toClient.flush();
            System.out.println("[ConfLoader] Response successfully flushed to client.");

        } catch (Exception e) {
            System.out.println("[ConfLoader] Exception caught during processing:");
            e.printStackTrace();
            sendErrorResponse(toClient, 500, "Internal Server Error - Failed to parse configuration blueprint");
        }
    }

    // Extracts structural configuration content from multi-part boundary streams
    private String extractCleanConfig(String rawBody) {
        if (!rawBody.contains("Content-Type:")) {
            return rawBody.trim();
        }
        
        String[] lines = rawBody.split("\r?\n");
        StringBuilder sb = new StringBuilder();
        boolean dataStarted = false;

        for (String line : lines) {
            if (dataStarted) {
                if (line.trim().startsWith("------")) { 
                    break;
                }
                sb.append(line).append("\n");
            } else {
                if (line.trim().startsWith("Content-Type:")) {
                    dataStarted = true; 
                }
            }
        }
        
        String result = sb.toString().trim();
        
        if (result.contains("\n")) {
            while (result.startsWith("\n") || result.startsWith("\r")) {
                result = result.substring(1);
            }
        }
        return result;
    }

    // Sends formatted HTTP error code pages back to connection stream
    private void sendErrorResponse(OutputStream out, int statusCode, String statusText) throws IOException {
        String body = "<h1>" + statusCode + " " + statusText + "</h1>";
        String response = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n" +
                          "Content-Type: text/html\r\n" +
                          "Content-Length: " + body.getBytes().length + "\r\n" +
                          "Connection: close\r\n" +
                          "\r\n" +
                          body;
        out.write(response.getBytes());
        out.flush();
    }

    @Override
    public void close() throws IOException {
    }
}