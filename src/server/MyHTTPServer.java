package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import server.RequestParser.RequestInfo;
import servlets.Servlet;

public class MyHTTPServer extends Thread implements HTTPServer {
    
    private final int port;
    private final int nThreads;
    
    private final ConcurrentHashMap<String, Servlet> getHandlers;
    private final ConcurrentHashMap<String, Servlet> postHandlers;
    private final ConcurrentHashMap<String, Servlet> deleteHandlers;
    
    private ExecutorService threadPool;
    private ServerSocket serverSocket;
    private volatile boolean running;

    public MyHTTPServer(int port, int nThreads) {
        this.port = port;
        this.nThreads = nThreads;
        this.getHandlers = new ConcurrentHashMap<>();
        this.postHandlers = new ConcurrentHashMap<>();
        this.deleteHandlers = new ConcurrentHashMap<>();
        this.running = false;
    }

    // Resolves the correct router mapping based on the HTTP command verb
    private ConcurrentHashMap<String, Servlet> getMapForCommand(String httpCommand) {
        if (httpCommand == null) return null;
        switch (httpCommand.toUpperCase()) {
            case "GET":
                return getHandlers;
            case "POST":
                return postHandlers;
            case "DELETE":
                return deleteHandlers;
            default:
                return null;
        }
    }

    @Override
    public void addServlet(String httpCommanmd, String uri, Servlet s) {
        ConcurrentHashMap<String, Servlet> map = getMapForCommand(httpCommanmd);
        if (map != null) {
            map.put(uri, s);
        }
    }

    @Override
    public void removeServlet(String httpCommanmd, String uri) {
        ConcurrentHashMap<String, Servlet> map = getMapForCommand(httpCommanmd);
        if (map != null) {
            map.remove(uri);
        }
    }

    @Override
    public void start() {
        this.running = true;
        super.start(); 
    }

    // Server socket connection listener loop managing thread pool execution
    @Override
    public void run() {
        this.threadPool = Executors.newFixedThreadPool(nThreads); 
        
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000); 
            
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setSoTimeout(2000); 
                    threadPool.execute(() -> handleClient(clientSocket));
                } catch (SocketTimeoutException e) {
                    // Loop back on timeout check
                } catch (IOException e) {
                    if (!running) break; 
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Parses the socket payload and matches incoming requests to target servlets
    private void handleClient(Socket clientSocket) {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream toClient = clientSocket.getOutputStream()
        ) {
            RequestInfo ri = RequestParser.parseRequest(reader);
            if (ri == null) {
                sendErrorResponse(toClient, 400, "Bad Request");
                return;
            }

            ConcurrentHashMap<String, Servlet> map = getMapForCommand(ri.getHttpCommand());
            Servlet matchedServlet = null;

            if (map != null) {
                matchedServlet = findLongestPrefixMatch(map, ri.getUri());
            }

            if (matchedServlet != null) {
                matchedServlet.handle(ri, toClient); 
            } else {
                sendErrorResponse(toClient, 404, "Not Found");
            }

        } catch (SocketTimeoutException e) {
            // Drop sluggish connection streams safely
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Matches endpoints dynamically choosing the most specific prefix mapping
    private Servlet findLongestPrefixMatch(ConcurrentHashMap<String, Servlet> map, String requestUri) {
        Servlet bestMatch = null;
        int longestLength = -1;

        String cleanUri = requestUri;
        int qIdx = requestUri.indexOf('?');
        if (qIdx != -1) {
            cleanUri = requestUri.substring(0, qIdx);
        }

        for (Map.Entry<String, Servlet> entry : map.entrySet()) {
            String registeredUri = entry.getKey();
            if (cleanUri.startsWith(registeredUri)) {
                if (registeredUri.length() > longestLength) {
                    longestLength = registeredUri.length();
                    bestMatch = entry.getValue();
                }
            }
        }
        return bestMatch;
    }

    // Generates generic fallback HTTP error pages back to the socket pipeline
    private void sendErrorResponse(OutputStream out, int statusCode, String statusText) throws IOException {
        String body = "<h1>" + statusCode + " " + statusText + "</h1>";
        String response = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n" +
                          "Content-Type: text/html\r\n" +
                          "Content-Length: " + body.getBytes().length + "\r\n" +
                          "\r\n" +
                          body;
        out.write(response.getBytes());
        out.flush();
    }

    // Triggers full system shutdown closing sockets and nested servlet allocations
    @Override
    public void close() {
        this.running = false;
        try {
            closeServletsInMap(getHandlers);
            closeServletsInMap(postHandlers);
            closeServletsInMap(deleteHandlers);

            if (threadPool != null) {
                threadPool.shutdownNow(); 
            }

            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Safely shuts down and removes every servlet item bound within a route map
    private void closeServletsInMap(ConcurrentHashMap<String, Servlet> map) {
        for (Servlet servlet : map.values()) {
            try {
                servlet.close(); 
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        map.clear();
    }
}