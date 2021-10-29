package programs;

import java.io.*;
import java.net.*;
  
// Server class
public class Server {

    private final int port;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private DataInputStream input;
    private DataOutputStream output;

    Server(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(this.port);
    }

    private Socket waitConnection() throws IOException {
        clientSocket = serverSocket.accept();
        return clientSocket;
    }

    private void treatConnection(Socket clientSocket) throws IOException, SocketException {
        try{
            // Obtaining input and output streams
            input = new DataInputStream(clientSocket.getInputStream());
            output = new DataOutputStream(clientSocket.getOutputStream());
            
            // Create a new thread object
            Thread thread = new ClientHandler(clientSocket, input, output);
            System.out.println("Assigning new thread for this client");

            // Invoking the start() method
            thread.start();
        } catch (Exception error){
            clientSocket.close();
            System.out.println(clientSocket + "connection reset by client");
            //error.printStackTrace();
        }
    }
    
    public void run() throws IOException {
        /* Running infinite loop for getting client request */
        while (true) {
            waitConnection();
            System.out.println("A new client is connected : " + clientSocket); 
            treatConnection(clientSocket);
        }
    }
}