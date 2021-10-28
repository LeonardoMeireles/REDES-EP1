package programs;

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

    public void startAuctionHouse() throws FileNotFoundException, IOException{
        StringBuilder fullLogo = escreveASC("resources/dragon.txt");
        fullLogo.append("\nWelcome traveler, what brings you to this place of wonders? Buying? Selling? Suit yourself! There's a place for everyone!\n");
        fullLogo.append("\n\nInsert your username:");
        dataOutputStream.writeUTF(fullLogo.toString());
        dataOutputStream.flush();
        String username = dataInputStream.readUTF();
        login(username);
    }

    //Not finished
    public void createAccount() throws FileNotFoundException, IOException{
        StringBuilder createAccountString = escreveASC("resources/create_account.txt");
        createAccountString.append("Insert your username:\n");
        String newUsername = dataInputStream.readUTF();
        createAccountString.append("Insert your password:\n");
        new File("resourcces/database"+newUsername).mkdirs();
    }

    public void login(String username) throws FileNotFoundException, IOException{
        File dataBase = new File("database");
        File [] list = dataBase.listFiles();

        //search for username && create new account if username is not found
        boolean userFound = false;
        for(File dir : list){
            String usernameFile = dir.getName();
            if( (username+".txt").equals(usernameFile)){
                userFound = true;
            }
        }
        if(userFound){
            dataOutputStream.writeUTF("Insert your password:");
            dataOutputStream.flush();
            String password = dataInputStream.readUTF();
        } else { //no user found
            dataOutputStream.writeUTF("\nHuum, i don't know any adventurer with that name, would you like to sign up for my services?\nY: yes\tN: no\n");
            dataOutputStream.flush();
            String answer = dataInputStream.readUTF();
            if(answer.toLowerCase().equals("y")){
                createAccount();
            } else{
                dataOutputStream.writeUTF("Insert your username:\n");
                dataOutputStream.flush();
                String newUsername = dataInputStream.readUTF();
                login(newUsername);
            }
        }
        
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
        System.out.println(name + price);
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
        String received;
        while (true){
            try{
                
                startAuctionHouse();
                
                received = dataInputStream.readUTF();
                //login(received);
                
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
            this.dataInputStream.close();
            this.dataOutputStream.close();
        } catch(IOException error){
            error.printStackTrace();
        }
    }
}