/***
 * EchoServer
 * Example of a TCP server
 * Date: 10/01/04
 * Authors:
 */

package stream;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class EchoServer  {
	private final Integer port;
	private static List<EchoServerThread> clients;
	private PrintWriter writer;

	public List<EchoServerThread> getClients(){
		return clients;
	}

	public EchoServer (Integer port) {
		this.port = port;
	}

	public PrintWriter getWriter() {
		return writer;
	}

	/**
  	* runServer method
  	* 
  	**/
    public void runServer() throws FileNotFoundException, UnsupportedEncodingException {
        ServerSocket listenSocket;

		clients = new ArrayList<EchoServerThread>();
		writer = new PrintWriter("chat.txt", "UTF-8");

		try {
			listenSocket = new ServerSocket(port); //port
			while (true) {
				Socket clientSocket = listenSocket.accept();
				System.out.println("connexion from:" + clientSocket.getInetAddress());

				EchoServerThread client = new EchoServerThread(this, clientSocket);
				Thread thread = new Thread(client);
				thread.start();
				clients.add(client);
			}
		} catch (Exception e) {
			System.err.println("Error in EchoServer:" + e);
		}

		writer.close();
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: java EchoServer <EchoServer port>");
			System.exit(1);
		}
		EchoServer server = new EchoServer(Integer.parseInt(args[0]));
		try {
			server.runServer();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}

  
