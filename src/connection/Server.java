package connection;

import database.SQLiteJDBC;

import java.io.*;
import java.util.*;
import java.net.*;

// connection.Server class
public class Server {

    static Vector<ClientHandler> activeClients = new Vector<>();
    static int activeClientsNum = 0;
    static SQLiteJDBC database = new SQLiteJDBC();

    public static void startThread(ClientHandler clientHandler){
        Thread thread = new Thread(clientHandler);

        System.out.println("Adding this client to active client list");

        // start the thread.
        thread.start();
    }

    public static void main(String[] args) throws IOException {
        // server is listening on port 1234
        ServerSocket serverSocket = new ServerSocket(1234);

        Socket socket;

        // loop receives client request
        
        while (true) {
            // wait for client connection
            socket = serverSocket.accept();
            System.out.println("New client request received : " + socket);

            // obtain input and output streams
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            System.out.println("Creating a new handler for this client...");

            // Create a new handler object for handling this request.
            ClientHandler clientHandler = new ClientHandler(socket, dataInputStream, dataOutputStream, database);

            // Create a new Thread with this object.
            startThread(clientHandler);
        }
    }
}