import database.SQLiteJDBC;
import programs.services.LoginAndRegister;

import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;


public class ClientHandler extends Thread {
    final public DataInputStream dataInputStream;
    final public DataOutputStream dataOutputStream;
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    DateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
    final Socket socket;
    static SQLiteJDBC database = new SQLiteJDBC();

    public ClientHandler(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        this.socket = socket;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
    }

    public void endConnection() throws IOException {
        System.out.println("Client " + this.socket + " sends exit...");
        System.out.println("Closing this connection.");
        this.socket.close();
        System.out.println("Connection closed");
    }

    public void startAuctionHouse() throws FileNotFoundException, IOException{
        StringBuilder fullLogo = escreveASC("resources/dragon.txt");
        fullLogo.append("\nWelcome traveler, what brings you to this place of wonders? Buying? Selling? Suit yourself! There's a place for everyone!\n");
        fullLogo.append("\n\nInsert your username:");
        dataOutputStream.writeUTF(fullLogo.toString());
        dataOutputStream.flush();
        String username = dataInputStream.readUTF();
        LoginAndRegister loginService = new LoginAndRegister(this);
        loginService.login(username);
    }

    public void addToAuction(){
        Date date = new Date();
        String name = "";
        String price = "";
        String rightNowDate =  dateFormat.format(date);
        String rightNowTime = timeFormat.format(date);

        try{
            dataOutputStream.writeUTF("Name: ");
            dataOutputStream.flush();
            name = dataInputStream.readUTF();
            if(name.equals("Cancel")){
                return;
            }

            dataOutputStream.writeUTF("Price: ");
            dataOutputStream.flush();
            price = dataInputStream.readUTF();
            if(price.equals("Cancel")){
                return;
            } 
        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    public StringBuilder escreveASC(String path) throws IOException{
        File file = new File(path);
        Scanner reader = new Scanner(file);
        StringBuilder fileStringBuilder = new StringBuilder();
        while(reader.hasNextLine()){
            fileStringBuilder.append(reader.nextLine()).append("\n");
        }
        reader.close();
        return fileStringBuilder;
    }

    @Override
    public void run(){
        database.connect();
        String received;
        try{
            startAuctionHouse();
        } catch(IOException error) {
            error.printStackTrace();
        }
        while (true){
            try{

                dataOutputStream.writeUTF("Please insert a command.");
                received = dataInputStream.readUTF();
                //login(received);
                
                if(received.equals("Exit")){
                    endConnection();
                    break;
                }
                
                // write on output stream based on the
                // answer from the client
                switch (received){
                    case "List my shop" :
                        break;

                    case "Auction" :
                        dataOutputStream.writeUTF("Please enter the item's name and price!\n");
                        dataOutputStream.flush();
                        addToAuction();
                        break;
                          
                    case "Remove Item" :
                        dataOutputStream.writeUTF("Time");
                        dataOutputStream.flush();
                        break;

                    case "List proposals" :
                        break;

                    case "Item X detail" :
                        break;

                    default:
                        dataOutputStream.writeUTF("Invalid input");
                        dataOutputStream.flush();
                        break;
                }
            } catch (IOException error) {
                error.printStackTrace();
            }
        }

        try{
            // closing resources
            database.disconnect();
            this.dataInputStream.close();
            this.dataOutputStream.close();
        } catch(IOException error){
            error.printStackTrace();
        }
    }
}