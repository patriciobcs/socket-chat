/***
 * EchoClient
 * Example of a TCP client 
 * Date: 10/01/04
 * Authors:
 */
package stream;

import javax.swing.*;
import java.io.*;
import java.net.*;

public class EchoClient {
    private String name;
    private App app;
    private String host;
    private Integer port;

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
        Socket echoSocket = null;
        Thread thIn = null;
        Thread thOut = null;

        //BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        //System.out.println("Insert your name: ");
        //setName(stdIn.readLine());

        try {
            // creation socket ==> connexion
            echoSocket = new Socket(host, port);
            EchoClientThread echoClientThreadOut = new EchoClientThread(this, echoSocket, "out");
            EchoClientThread echoClientThreadIn = new EchoClientThread(this, echoSocket, "in");
            thOut = new Thread(echoClientThreadOut);
            thIn = new Thread(echoClientThreadIn);
            thOut.start();
            thIn.start();
            thOut.join();
            thIn.join();
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + host);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for " + "the connection to:"+ host);
            System.exit(1);
        }

        echoSocket.close();
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


