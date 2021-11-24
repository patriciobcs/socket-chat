/***
 * EchoClient
 * Example of a TCP client 
 * Date: 10/01/04
 * Authors:
 */
package chat;

import javax.swing.*;
import java.io.*;
import java.net.*;

public class EchoClient {
    private String name;
    private App app;
    private String host;
    private Integer port;
    private PrintStream socOut = null;
    private Socket unicastSocket = null;
    private MulticastSocket multicastSocket = null;

    public EchoClient(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public App getApp() { return app; }
    public void setApp(App app) { this.app = app; }
    public void setHost(String host) { this.host = host; }

    public void runClientMulticast() throws IOException, InterruptedException {
        try {
            multicastSocket = new MulticastSocket(port);
            multicastSocket.joinGroup(InetAddress.getByName(host));
            EchoClientThread echoClientThreadIn = new EchoClientThread(this, multicastSocket);
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
                multicastSocket.close();
            }
        });
    }

    public void runClientUnicast() throws IOException, InterruptedException {
        try {
            // creation socket ==> connexion
            unicastSocket = new Socket(host, port);
            socOut = new PrintStream(unicastSocket.getOutputStream());
            EchoClientThread echoClientThreadIn = new EchoClientThread(this, unicastSocket);
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
                    unicastSocket.close();
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
        System.out.println(message);
        try {
            if (unicastSocket != null) socOut.println(message);
            else if (multicastSocket != null) {
                DatagramPacket datagram = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName(host), port);
                multicastSocket.send(datagram);
            }
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


