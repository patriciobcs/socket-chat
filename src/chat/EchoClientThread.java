package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.Objects;

public class EchoClientThread implements Runnable {
    private EchoClient client;
    private Socket unicastSocket = null;
    private MulticastSocket multicastSocket = null;

    public EchoClientThread(EchoClient client, Socket unicastSocket) {
        this.client = client;
        this.unicastSocket = unicastSocket;
    }

    public EchoClientThread(EchoClient client, MulticastSocket multicastSocket) {
        this.client = client;
        this.multicastSocket = multicastSocket;
    }

    public void unicast() throws IOException {
        BufferedReader socIn = new BufferedReader(new InputStreamReader(unicastSocket.getInputStream()));
        while (!unicastSocket.isClosed()) {
            String message = socIn.readLine();
            if (message != null) {
                System.out.println(message);
                client.getApp().addMessage(message);
            } else break;
        }
        socIn.close();
    }

    public void multicast() throws IOException {
        while (!multicastSocket.isClosed()) {
            byte[] buf = new byte[1000];
            DatagramPacket datagram = new DatagramPacket(buf, buf.length);
            multicastSocket.receive(datagram);
            String message = new String(buf,0,datagram.getLength(),"UTF-8");
            if (message != null) {
                if (message.length() > 25) {
                    String username = message.substring(22, message.indexOf(":", 22));
                    if (!Objects.equals(username, client.getName())) client.getApp().addMessage(message);
                }
            } else break;
        }
    }

    @Override
    public void run() {
        try {
            if (unicastSocket != null) unicast();
            else if (multicastSocket != null) multicast();
            else throw new Exception("Socket not initialized");
            System.out.println("EchoClientTread stopped");
        } catch (Exception e) {
            System.err.println("Error in EchoClientTread:" + e);
            e.printStackTrace();
        }
    }
}