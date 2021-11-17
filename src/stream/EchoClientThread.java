package stream;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class EchoClientThread implements Runnable {
    private EchoClient client;
    private Socket socket;

    public EchoClientThread(EchoClient client, Socket socket) {
        this.client = client;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader socIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (!socket.isClosed()) {
                String line = socIn.readLine();
                if (line != null) {
                    System.out.println(line);
                    client.getApp().addMessage(line);
                } else break;
            }
            System.out.println("break");
            socIn.close();
        } catch (Exception e) {
            System.err.println("Error in EchoClientTread:" + e);
            e.printStackTrace();
        }
    }
}