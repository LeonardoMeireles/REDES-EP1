import java.io.*;
import java.net.*;
  
// Server class
public class Server 
{
    public static void main(String[] args) throws IOException 
    {
        // server is listening on port 5056
        ServerSocket serverSocket = new ServerSocket(5056);
          
        // running infinite loop for getting
        // client request
        while (true){
            Socket socket = null;
              
            try{
                // socket object to receive incoming client requests
                socket = serverSocket.accept();
                  
                System.out.println("A new client is connected : " + socket);
                  
                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                  
                System.out.println("Assigning new thread for this client");
  
                // create a new thread object
                Thread t = new ClientHandler(socket, dis, dos);
  
                // Invoking the start() method
                t.start();
                  
            }
            catch (Exception error){
                socket.close();
                error.printStackTrace();
            }
        }
    }
}