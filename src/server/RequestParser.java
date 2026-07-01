package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestParser {

    // Processes standard HTTP network socket input streams into clean RequestInfo objects
    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {        
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            return null;
        }

        String[] parts = requestLine.split(" ");
        if (parts.length < 2) {
            return null;
        }

        String httpCommand = parts[0];
        String uri = parts[1]; 
        Map<String, String> parameters = new HashMap<>();

        int questionMarkIndex = uri.indexOf('?');
        if (questionMarkIndex != -1) {
            String queryString = uri.substring(questionMarkIndex + 1);
            parseQueryString(queryString, parameters);
        }

        String cleanUriForSegments = questionMarkIndex != -1 ? uri.substring(0, questionMarkIndex) : uri;
        String[] rawSegments = cleanUriForSegments.split("/");
        List<String> cleanedSegments = new ArrayList<>();
        for (String segment : rawSegments) {
            if (!segment.isEmpty()) {
                cleanedSegments.add(segment);
            }
        }
        String[] uriSegments = cleanedSegments.toArray(new String[0]);

        String headerLine;
        int contentLength = 0;
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            if (headerLine.toLowerCase().startsWith("content-length:")) {
                contentLength = Integer.parseInt(headerLine.substring(15).trim());
            }
        }

        byte[] content = new byte[0];
        
        if (contentLength > 0) {
            char[] buffer = new char[contentLength];
            int totalRead = 0;
            while (totalRead < contentLength) {
                int read = reader.read(buffer, totalRead, contentLength - totalRead);
                if (read == -1) {
                    break; 
                }
                totalRead += read;
            }
            
            String bodyString = new String(buffer, 0, totalRead);
            
            if (bodyString.contains("=") && !bodyString.contains("Content-Type:")) {
                parseQueryString(bodyString, parameters);
            }
            
            content = bodyString.getBytes();
        }
        
        return new RequestInfo(httpCommand, uri, uriSegments, parameters, content);
    }

    // Tokenizes parameter fields separating standard key-value query items
    private static void parseQueryString(String queryString, Map<String, String> parameters) {
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                parameters.put(kv[0], kv[1]);
            } else if (kv.length == 1) {
                parameters.put(kv[0], "");
            }
        }
    }
	
    public static class RequestInfo {
        private final String httpCommand;
        private final String uri;
        private final String[] uriSegments;
        private final Map<String, String> parameters;
        private final byte[] content;

        public RequestInfo(String httpCommand, String uri, String[] uriSegments, Map<String, String> parameters, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            this.uriSegments = uriSegments;
            this.parameters = parameters;
            this.content = content;
        }

        public String getHttpCommand() { return httpCommand; }
        public String getUri() { return uri; }
        public String[] getUriSegments() { return uriSegments; }
        public Map<String, String> getParameters() { return parameters; }
        public byte[] getContent() { return content; }
    }
}