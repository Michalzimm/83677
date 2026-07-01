package views;

import server.HTTPServer;
import server.MyHTTPServer;
import servlets.TopicDisplayer;
import servlets.ConfLoader;
import servlets.HtmlLoader;

public class Main {
    
    public static void main(String[] args) throws Exception {
        HTTPServer server = new MyHTTPServer(8080, 5);

        server.addServlet("GET", "/publish", new TopicDisplayer());
        server.addServlet("POST", "/upload", new ConfLoader());
        server.addServlet("GET", "/app/", new HtmlLoader("html_files"));

        server.start();
        System.out.println("Server started on port 8080...");

        // Replacing System.in.read() with an infinite loop to keep the server alive stable
        while (true) {
            try {
                Thread.sleep(10000); // Sleep for 10 seconds every iteration
            } catch (InterruptedException e) {
                break; 
            }
        }
    }
}