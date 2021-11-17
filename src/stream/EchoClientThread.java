package stream;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EchoClientThread implements Runnable {
    private EchoClient client;
    private Socket socket;
    private String mode;
    private static boolean on = false;

    public Socket getSocket() {
        return socket;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public EchoClientThread(EchoClient client, Socket socket, String mode) {
        this.client = client;
        this.socket = socket;
        this.mode = mode;
        this.on = true;
    }

    public void receive() {
        try {
            BufferedReader socIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (!socket.isClosed() && on) {
                String line = socIn.readLine();
                if (line != null) System.out.println(line);
                else break;
            }
            socIn.close();
        } catch (Exception e) {
            //System.err.println("Error in EchoClientTread:" + e);
            //e.printStackTrace();
        }
    }

    public void send() {
        try {
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            PrintStream socOut = new PrintStream(socket.getOutputStream());
            String line;

            while (on) {
                line = stdIn.readLine();
                if (line.equals(".")) break;
                String uuid = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
                line = uuid + " - " + client.getName() + ": " + line;
                socOut.println(line);
                System.out.println(line);
            }
            on = false;
            socOut.close();
            stdIn.close();
        } catch (Exception e) {
            System.err.println("Error in EchoClientTread:" + e);
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (mode == "out") send();
        else receive();
    }
}