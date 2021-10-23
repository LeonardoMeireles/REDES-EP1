import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;

class ClientHandler extends Thread {
    final DataInputStream dataInputStream;
    final DataOutputStream dataOutputStream;
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    DateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
    final Socket socket;
      
    public ClientHandler(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        this.socket = socket;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
    }
    
    public void writeLogo() throws FileNotFoundException, IOException{
        File logo = new File("./dragon.txt");
        Scanner reader = new Scanner(logo);
        StringBuilder fullLogo = new StringBuilder("\n");
        while(reader.hasNextLine()){
            fullLogo.append(reader.nextLine()).append("\n");
        }
        fullLogo.append("\n\n\n\nPlease login!\n");
        dataOutputStream.writeUTF(fullLogo.toString());
        reader.close();
    }

    public void addToAuction(){
        Date date = new Date();
        String name = "";
        String price = "";
        String rightNowDate =  dateFormat.format(date);
        String rightNowTime = timeFormat.format(date);

        try{
            dataOutputStream.writeUTF("Name: ");
            name = dataInputStream.readUTF();
            if(name.equals("Cancel")){
                return;
            }

            dataOutputStream.writeUTF("Price: ");
            price = dataInputStream.readUTF();
            if(price.equals("Cancel")){
                return;
            } 
        } catch (IOException error) {
            error.printStackTrace();
        }
        System.out.println(name + price);
    }
  
    @Override
    public void run(){
        String received;
        while (true){
            try{
                // Ask user what he wants
                writeLogo();
                // ===================== TO DO =====================
                // userLogin();
                // dataOutputStream.writeUTF("Welcome traveler, what brings you to this place of wonders? Buying? Selling? Suit yourself! There's a place for everyone!\n");
                // receive the answer from client
                received = dataInputStream.readUTF();
                
                if(received.equals("Exit")){ 
                    System.out.println("Client " + this.socket + " sends exit..."); 
                    System.out.println("Closing this connection.");
                    this.socket.close();
                    System.out.println("Connection closed");
                    break;
                }
                
                // write on output stream based on the
                // answer from the client
                switch (received){
                    case "List my shop" :
                        break;

                    case "Auction" :
                        dataOutputStream.writeUTF("Please enter the item's name and price!\n");
                        addToAuction();
                        break;
                          
                    case "Remove Item" :
                        dataOutputStream.writeUTF("Time");
                        break;

                    case "Time" :
                        break;

                    case "List proposals" :
                        break;
                    
                    case "Item X detail" :
                        break;

                    default:
                        dataOutputStream.writeUTF("Invalid input");
                        break;
                }
            } catch (IOException error) {
                error.printStackTrace();
            }
        }

        try{
            // closing resources
            this.dataInputStream.close();
            this.dataOutputStream.close();
        } catch(IOException error){
            error.printStackTrace();
        }
    }
}