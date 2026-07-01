# Exercise 5 - Web Computational Graph Server

## Background
This project implements a multi-threaded HTTP Server designed to manage and visualize a computational pipeline graph. The architecture integrates a custom network infrastructure (MyHTTPServer, RequestParser) with web servlet handlers (ConfLoader, TopicDisplayer, HtmlLoader) and an underlying messaging pipeline based on the Publish-Subscribe pattern (TopicManager, ParallelAgent, BinOpAgent). The application parses structural topology configurations, processes continuous dynamic token streams, and displays computational state summaries in real-time.

## Installation
1. Ensure you have Java JDK 11 or higher installed on your system.
2. Clone this repository or download the source files:
   git clone https://github.com/Michalzimm/83677.git
3. Import the project into your preferred IDE (e.g., Eclipse) as a standard Java project.
4. Ensure the html_files/ directory is situated in the root folder alongside the source directory.

## Execution
To launch the computational server application:
1. Locate and run the main entry point class: server.Main (or Main.java).
2. The server will initialize and bind to the designated port:
   Server started on port 8080...
3. Open your web browser and navigate to the local landing view:
   http://localhost:8080/app/index.html

---

## Current Status & Known Issues
Due to integration challenges encountered late in the development cycle, certain dynamic cascading behaviors do not fully compute across nested nodes under all execution circumstances. Consequently, the project is submitted in its current stable functional layout. 

**Working Features:**
* The custom HTTP server initializes and establishes network bindings successfully.
* The frontend layout resources load properly, and the visual computational graph parses and renders correctly upon deployment.
* Publishing parameter values directly via the input web forms successfully pushes data to the streaming pipeline and updates the summary tracking tables immediately (as illustrated in the Output log references).
