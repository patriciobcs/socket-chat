/***
 * EchoClient
 * Example of a TCP client 
 * Date: 10/01/04
 * Authors:
 */
package stream.chat;

import javax.swing.*;
import java.io.*;
import java.net.*;

public class EchoClient {
    private String name;
    private App app;
    private String host;
    private Integer port;
    private Socket echoSocket = null;
    private PrintStream socOut = null;

    public EchoClient(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public App getApp() { return app; }
    public void setApp(App app) { this.app = app; }

    public void runClient() throws IOException, InterruptedException {
        try {
            // creation socket ==> connexion
            echoSocket = new Socket(host, port);
            socOut = new PrintStream(echoSocket.getOutputStream());
            EchoClientThread echoClientThreadIn = new EchoClientThread(this, echoSocket);
            Thread thIn = new Thread(echoClientThreadIn);
            thIn.start();
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + host);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for " + "the connection to:"+ host);
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                try {
                    echoSocket.close();
                    socOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * send message to the client through sockets
     * @param message
     */
    public void send(String message) {
        try {
            socOut.println(message);
            System.out.println(message);
        } catch (Exception e) {
            System.err.println("Error in EchoClientTread:" + e);
            e.printStackTrace();
        }
    }

    /**
     *  main method
     *  accepts a connection, receives a message from client then sends an echo to the client
     **/
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java EchoClient <EchoServer host> <EchoServer port>");
            System.exit(1);
        }

        EchoClient client = new EchoClient(args[0], Integer.parseInt(args[1]));

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager
                            .getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                App app = new App();
                app.init();
                app.setClient(client);
                client.setApp(app);
            }
        });
    }
}


