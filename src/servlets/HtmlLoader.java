package servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import servlets.Servlet;
import server.RequestParser.RequestInfo;

public class HtmlLoader implements Servlet {

    private final String htmlFilesDir;

    // Normalizes file directory mapping adding trailing slashes when omitted
    public HtmlLoader(String htmlFilesDir) {
        if (!htmlFilesDir.endsWith("/")) {
            this.htmlFilesDir = htmlFilesDir + "/";
        } else {
            this.htmlFilesDir = htmlFilesDir;
        }
    }

    // Resolves file system paths and streams static web assets back to browser
    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        String[] segments = ri.getUriSegments();
        String fileName = "index.html"; 

        if (segments != null && segments.length > 1) {
            fileName = segments[segments.length - 1];
        }

        Path filePath = Paths.get(this.htmlFilesDir + fileName);

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            sendErrorResponse(toClient, 404, "File Not Found - The requested HTML resource does not exist.");
            return;
        }

        byte[] fileBytes = Files.readAllBytes(filePath);

        String contentType = "text/html";
        if (fileName.endsWith(".css")) {
            contentType = "text/css";
        } else if (fileName.endsWith(".js")) {
            contentType = "text/javascript";
        }

        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                              "Content-Type: " + contentType + "\r\n" +
                              "Content-Length: " + fileBytes.length + "\r\n" +
                              "\r\n";
        
        toClient.write(httpResponse.getBytes());
        toClient.write(fileBytes);
        toClient.flush();
    }

    // Dispatches clear 404/500 web content error blocks
    private void sendErrorResponse(OutputStream out, int statusCode, String statusText) throws IOException {
        String body = "<!DOCTYPE html><html><head><title>Error</title></head><body>" +
                      "<h1>" + statusCode + " " + statusText + "</h1>" +
                      "</body></html>";
        byte[] bodyBytes = body.getBytes();
        
        String response = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n" +
                          "Content-Type: text/html\r\n" +
                          "Content-Length: " + bodyBytes.length + "\r\n" +
                          "\r\n";
        out.write(response.getBytes());
        out.write(bodyBytes);
        out.flush();
    }

    @Override
    public void close() throws IOException {
    }
}