package stream.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;
import org.json.*;

/**
 * Server HTTP for network programming TP based on WebServer from Chapter 1
 * Programming Spiders, Bots and Aggregators in Java Copyright 2001 by Jeff Heaton
 * 
 * @author Patricio Calderon
 * @version 1.0
 */
public class WebServer {
  /**
   * WebServer constructor.
   */
  protected void start() {
    ServerSocket s;

    System.out.println("Webserver starting up on port 80");
    System.out.println("(press ctrl-c to exit)");
    try {
      // create the main server socket
      s = new ServerSocket(3000);
    } catch (Exception e) {
      System.out.println("Error: " + e);
      return;
    }

    System.out.println("Waiting for connection");
    for (;;) {
      try {
        // Wait for a connection
        Socket remote = s.accept();
        // Remote is now the connected socket
        System.out.println("Connection");

        // Set output stream
        OutputStream out = remote.getOutputStream();
        // Set input stream
        InputStreamReader in = new InputStreamReader(remote.getInputStream());
        BufferedReader br = new BufferedReader(in);

        // Set HTTP params
        String[] requestCols, queries = null;
        String method = null, uri = null;
        String headerLine;

        do {
          headerLine = br.readLine();
          if (headerLine != null && headerLine.contains("HTTP") && method == null) {
            requestCols = headerLine.split("\\s");
            method = requestCols[0].toUpperCase();
            uri = requestCols[1];
            queries = uri.length() > 0 ? uri.split("/") : new String[]{};
            System.out.println("Method:\t " + method);
            System.out.println("URI:\t " + uri.length());
          }
        } while (headerLine != null && headerLine.length() != 0);

        if (method == null) continue;

        // Set payload
        StringBuilder payloadBuilder = new StringBuilder();
        while(br.ready()){ payloadBuilder.append((char) br.read());}
        JSONObject payload = new JSONObject(payloadBuilder.length() > 0 ? payloadBuilder.toString().trim() : "{}");
        System.out.println("Payload: " + payload);

        // REST methods
        switch (method) {
          case "GET" -> {
            // Return Adder (index) HTML
            if (Objects.equals(uri, "/") || Objects.equals(uri, "")) {
              sendTextFile(out, "adder.html", "text/html");
              // Return others resources
            } else if (queries.length > 1) {
              // Return HTMLs
              if (queries[queries.length - 1].endsWith(".html")) {
                sendTextFile(out, queries[1], "text/html");
              } else if (queries[queries.length - 1].endsWith(".json")) {
                sendTextFile(out, queries[1], "application/json;charset=UTF-8");
                // Return Images
              } else {
                sendImage(out, queries[1]);
              }
            }
          }
          case "POST" -> {
            JSONObject response = new JSONObject();
            if (queries.length > 1 && queries[1].endsWith(".json") && payload.length() > 0) {
              response = addFile(queries[1], payload.toString());
            } else if (queries.length > 1 && Objects.equals(queries[1], "sum") &&
                    payload.has("a") && payload.has("b")) {
              double a = payload.getInt("a"), b = payload.getInt("b");
              response.put("success", true);
              response.put("c", a + b);
            } else {
              response.put("success", false);
              response.put("error", "500 Internal Server Error");
            }
            sendJSON(out, response);
          }
          case "PUT" -> {
            JSONObject response = new JSONObject();
            if (queries.length > 1 && queries[1].endsWith(".json") && payload.length() > 0) {
              response = updateFile(queries[1], payload.toString());
            }
            sendJSON(out, response);
          }
          case "DELETE" -> {
            JSONObject response = new JSONObject();
            if (queries.length > 1 && queries[1].endsWith(".json")) {
              response = removeFile(queries[1]);
            }
            sendJSON(out, response);
          }
        }

        remote.close();
      } catch (Exception e) {
        System.out.println("Error: " + e);
        e.printStackTrace();
      }
    }
  }

  /**
   * Send text file (HTML, JSON, etc) through sockets
   * @param out
   * @param filename
   * @throws IOException
   */
  public void sendTextFile(OutputStream out, String filename, String contentType) throws IOException {
    PrintWriter pwOut = new PrintWriter(out);

    try {
      BufferedReader br = new BufferedReader(new FileReader(filename));

      // Send the HTTP headers
      pwOut.println("HTTP/1.0 200 OK");
      pwOut.println("Content-Type: " + contentType);
      pwOut.println("Server: PR Web Server");
      pwOut.println();

      // Send the HTML content
      pwOut.println(br.lines().collect(Collectors.joining("\n")));
      br.close();
      pwOut.flush();
    } catch (FileNotFoundException e) {
      fileNotFound(out);
    }
  }

  /**
   * Send image through sockets
   * @param out
   * @param filename
   * @throws IOException
   */
  public void sendImage(OutputStream out, String filename) throws IOException {
    DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(out));

    // Get file extension
    String[] filenameComponents = filename.split("\\.");
    if (filenameComponents.length < 2) return;
    String extension = filenameComponents[filenameComponents.length-1];

    // Get file content
    byte[] bytes = new byte[1024];
    try {
      InputStream in = new FileInputStream(filename);
      int count;

      // Send the HTTP headers
      writer.writeBytes("HTTP/1.0 200 OK\r\n");
      writer.writeBytes("Content-Type: image/" + extension + "\r\n");
      writer.writeBytes("Server: PR Web Server\r\n");
      writer.writeBytes("\r\n");

      // Send image file content
      while ((count = in.read(bytes)) > 0) {
        writer.write(bytes, 0, count);
      }
      writer.flush();
    } catch (FileNotFoundException e) {
      fileNotFound(out);
    }
  }

  /**
   * Send 404 HTML file through sockets using 404 HTTP error
   * @param out
   * @throws IOException
   */
  public void fileNotFound(OutputStream out) throws IOException {
    PrintWriter pwOut = new PrintWriter(out);
    System.out.println("File not found");
    BufferedReader br = new BufferedReader(new FileReader("404.html"));

    // Send the HTTP headers
    pwOut.println("HTTP/1.0 404 Not Found");
    pwOut.println("Content-Type: text/html");
    pwOut.println("Server: PR Web Server");
    pwOut.println();

    // Send the HTML content
    pwOut.println(br.lines().collect(Collectors.joining("\n")));
    br.close();
    pwOut.flush();
  }

  /**
   * Create a new file with the filename and its content (payload)
   * @param filename
   * @param payload
   * @return
   */
  public JSONObject addFile(String filename, String payload) {
    File file = new File(filename);
    JSONObject response = new JSONObject();
    try {
      if (file.createNewFile()) {
        System.out.println("File created: " + file.getName());
        FileWriter writer = new FileWriter(file.getName());
        writer.write(payload);
        writer.close();
        response.put("success", true);
      } else {
        System.out.println("File already exists");
        response.put("success", false);
        response.put("error", "409 Conflict");
      }
    } catch (IOException e) {
      System.out.println("Error creating file");
      response.put("success", false);
      response.put("error", "500 Internal Server Error");
    }
    return response;
  }

  /**
   * Update a file using its new content (payload)
   * if the file doesn't exist, a new one is created
   * @param filename
   * @param payload
   * @return
   */
  public JSONObject updateFile(String filename, String payload) {
    JSONObject response = new JSONObject();
    try {
      FileWriter writer = new FileWriter(filename);
      writer.write(payload);
      writer.close();
      response.put("success", true);
    } catch (FileNotFoundException e) {
      response.put("success", false);
      response.put("error", "404 Not Found");
    } catch (IOException e) {
      System.out.println("Error creating file");
      response.put("success", false);
      response.put("error", "500 Internal Server Error");
    }
    return response;
  }

  /**
   * Remove a file using its filename
   * @param filename
   * @return
   */
  public JSONObject removeFile(String filename) {
    JSONObject response = new JSONObject();
    File file = new File(filename);
    if (!file.exists()) {
      response.put("success", false);
      response.put("error", "404 Not Found");
    } else if (file.delete()) {
      System.out.println("Deleted the file: " + file.getName());
      response.put("success", true);
    } else {
      System.out.println("Error creating file");
      response.put("success", false);
      response.put("error", "500 Internal Server Error");
    }
    return response;
  }

  /**
   * Send a JSON object through sockets
   * @param out
   * @param jsonObject
   * @throws IOException
   */
  public void sendJSON(OutputStream out, JSONObject jsonObject) throws IOException {
    PrintWriter pwOut = new PrintWriter(out);
    OutputStreamWriter dataOut = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    String json = jsonObject.toString();

    // Set status
    String status = "200 OK";
    if (!jsonObject.getBoolean("success")) status = jsonObject.getString("error");

    // Send the HTTP headers
    pwOut.println("HTTP/1.1 " + status);
    pwOut.println("Server: PR Web Server");
    pwOut.println("Date: " + new Date());
    pwOut.println("Content-type: application/json;charset=UTF-8");
    pwOut.println("Content-length: " + json.length());
    pwOut.println();
    pwOut.flush();

    // Sed JSON content
    dataOut.write(json);
    dataOut.flush();
  }

  /**
   * Start the application.
   * @param args
   *            Command line parameters are not used.
   */
  public static void main(String[] args) {
    WebServer ws = new WebServer();
    ws.start();
  }
}
