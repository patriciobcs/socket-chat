package chat;

import java.io.*;
import java.net.Socket;

public class EchoServerThread implements Runnable {
    private Socket socket;
    private EchoServer server;

    public Socket getSocket() {
        return socket;
    }

    public EchoServerThread(EchoServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    public void sendLastMessages() {
        BufferedReader br = null;
        try {
            File f = new File("doc/chat.txt");
            if (!f.exists()){
                f.createNewFile();
            }
            br = new BufferedReader(new FileReader(f));
            String line = null;
            PrintStream clientSocOut = new PrintStream(socket.getOutputStream());
            while (true) {
                line = br.readLine();
                if (line != null) clientSocOut.println(line);
                else break;
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * receives a request from client then sends an echo to the client
     **/
    @Override
    public void run() {
        try {
            BufferedReader socIn = null;
            socIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            sendLastMessages();
            while (!socket.isClosed()) {
                String line = socIn.readLine();
                if (line == null) break;
                server.getWriter().println(line);
                server.getWriter().flush();
                System.out.println("Message: " + line);
                for (EchoServerThread client : server.getClients()){
                    if (client.getSocket() != socket) {
                        PrintStream clientSocOut = new PrintStream(client.getSocket().getOutputStream());
                        clientSocOut.println(line);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in EchoServerThread:" + e);
        }
    }
}