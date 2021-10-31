package connection;

import database.SQLiteJDBC;

import java.io.*;
import java.util.*;
import java.net.*;

// connection.Server class
public class Server {

    static Vector<ClientHandler> loggedInClients = new Vector<>();
    static int loggedInClientsNum = 0;
    static SQLiteJDBC database = new SQLiteJDBC();

    public static void startThread(ClientHandler clientHandler){
        Thread thread = new Thread(clientHandler);
        thread.start();
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        Socket socket;

        while (true) {
            // Looking for new connections
            socket = serverSocket.accept();
            System.out.println("New client request received : " + socket);

            // Start input and output streams
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            ClientHandler clientHandler = new ClientHandler(socket, dataInputStream, dataOutputStream, database);

            // New thread for incoming client
            startThread(clientHandler);
        }
    }
}