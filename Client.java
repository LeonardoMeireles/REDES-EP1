import java.io.*;
import java.net.*;
import java.util.Scanner;
  
// Client class
public class Client {
    public static void main(String[] args) throws IOException{
        try{
            Scanner scanner = new Scanner(System.in);
              
            // getting localhost ip
            InetAddress ip = InetAddress.getByName("localhost");
      
            // establish the connection with server port 5056
            Socket socket = new Socket(ip, 5056);
      
            // obtaining input and out streams
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
      
            // the following loop performs the exchange of
            // information between client and client handler
            while (true){
                System.out.println(dataInputStream.readUTF());
                String tosend = scanner.nextLine();
                dataOutputStream.writeUTF(tosend);
                  
                // If client sends exit,close this connection 
                // and then break from the while loop
                if(tosend.equals("Exit")){
                    System.out.println("Closing this connection : " + socket);
                    socket.close();
                    System.out.println("Connection closed");
                    break;
                }
                  
                // printing date or time as requested by client
                String received = dataInputStream.readUTF();
                System.out.println(received);
            }
              
            // closing resources
            scanner.close();
            dataInputStream.close();
            dataOutputStream.close();
        } catch(Exception error){
            error.printStackTrace();
        }
    }
}