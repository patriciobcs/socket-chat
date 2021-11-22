///A Simple Web Server (WebServer.java)

package stream.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.json.*;

/**
 * Example program from Chapter 1 Programming Spiders, Bots and Aggregators in
 * Java Copyright 2001 by Jeff Heaton
 * 
 * WebServer is a very simple web-server. Any request is responded with a very
 * simple web-page.
 * 
 * @author Jeff Heaton
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
        // wait for a connection
        Socket remote = s.accept();
        // remote is now the connected socket
        System.out.println("Connection");

        OutputStream out = remote.getOutputStream();
        InputStreamReader in = new InputStreamReader(remote.getInputStream());
        BufferedReader br = new BufferedReader(in);
        Map<String, String> paramMap = new HashMap<String, String>();

        String inputLine = br.readLine();
        String[] requestCols = inputLine.split("\\s");
        paramMap.put("method", requestCols[0].toUpperCase());
        paramMap.put("uri", requestCols[1]);

        System.out.println("Method:\t" + paramMap.get("method"));
        System.out.println("URI:\t" + paramMap.get("uri"));

        String headerLine = null;
        while((headerLine = br.readLine()).length() != 0){}

        StringBuilder payloadBuilder = new StringBuilder();
        while(br.ready()){
          payloadBuilder.append((char) br.read());
        }
        System.out.println(payloadBuilder.length());
        JSONObject payload = new JSONObject(payloadBuilder.length() > 0 ? payloadBuilder.toString() : "{}");
        System.out.println("Payload: " + payload);

        switch (paramMap.get("method")) {
          case "GET": {
            if (Objects.equals(paramMap.get("uri"), "/") || Objects.equals(paramMap.get("uri"), "/adder.html")) {
              sendHtml(out, "adder.html");
            } else  {
              String[] querys = paramMap.get("uri").split("/");
              System.out.println(querys.length);
              if (querys.length == 2) {
                System.out.println(querys[1]);
                sendImage(out, querys[1]);
              }
            }
            break;
          }
          case "POST": {
            JSONObject response = new JSONObject();
            if (payload.has("a") && payload.has("b")) {
              double a = payload.getInt("a"), b = payload.getInt("b");
              response.put("success", true);
              response.put("c", a + b);
              sendJSON(out, response.toString());
            } else {
              response.put("success", false);
              sendJSON(out, response.toString());
            }
          }
        }

        remote.close();
      } catch (Exception e) {
        System.out.println("Error: " + e);
        e.printStackTrace();
      }
    }
  }

  public void sendHtml(OutputStream out, String filename) throws IOException {
    PrintWriter pwOut = new PrintWriter(out);
    // Send the response
    // Send the headers
    pwOut.println("HTTP/1.0 200 OK");
    pwOut.println("Content-Type: text/html");
    pwOut.println("Server: Bot");
    pwOut.println("");

    BufferedReader br = new BufferedReader(new FileReader("adder.html"));
    pwOut.println(br.lines().collect(Collectors.joining("\n")));
    br.close();

    pwOut.flush();
  }

  public void sendImage(OutputStream out, String filename) throws IOException {
    String[] filenameComponents = filename.split("\\.");
    if (filenameComponents.length < 2) return;
    String extension = filenameComponents[filenameComponents.length-1];

    byte[] bytes = new byte[1024];
    InputStream in = new FileInputStream(filename);
    int count;
    // Setting HTTP response headers
    DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(out));
    writer.writeBytes("HTTP/1.0 200 OK\r\n");
    writer.writeBytes("Content-Type: image/" + extension + "\r\n");
    writer.writeBytes("\r\n");

    while ((count = in.read(bytes)) > 0) {
      writer.write(bytes, 0, count);
    }
    writer.flush();
  }

  public void sendJSON(OutputStream out, String json) throws IOException {
    PrintWriter pwOut = new PrintWriter(out);
    OutputStreamWriter dataOut = new OutputStreamWriter(out, StandardCharsets.UTF_8);

    String content = "application/json;charset=UTF-8";
    int fileLength = json.length();

    // send HTTP Headers
    pwOut.println("HTTP/1.1 200 OK");
    pwOut.println("Server: Federico's Java Server");
    pwOut.println("Date: " + new Date());
    pwOut.println("Content-type: " + content);
    pwOut.println("Content-length: " + fileLength);
    pwOut.println(); // blank line between headers and content, very important !
    pwOut.flush(); // flush character output stream buffer
    dataOut.write(json);
    dataOut.flush();
  }

  /**
   * Start the application.
   * 
   * @param args
   *            Command line parameters are not used.
   */
  public static void main(String args[]) {
    WebServer ws = new WebServer();
    ws.start();
  }
}
