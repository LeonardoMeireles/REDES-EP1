package connection;

import database.SQLiteJDBC;
import services.LoginAndRegister;
import services.Market;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
    public static Connection connection;
    Statement statement;
    public String username;

    public ClientHandler(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream, SQLiteJDBC database) {
        this.socket = socket;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.connection = database.connect();
    }

    public void endConnection() throws IOException {
        System.out.println("User " +this.username +" is disconnecting");
        Server.loggedInClients.remove(this);
        Server.loggedInClientsNum--;
        this.socket.close();
        System.out.println("User " +this.username +" disconnected");
    }

    // Starts the intro for the app
    public void startAuctionHouse() throws SQLException, IOException{
        // Writes down the intro for the app
        StringBuilder fullLogo = writeASC("resources/dragon.txt");
        fullLogo.append("\nWelcome traveler, what brings you to this place of wonders? Buying? Selling? Suit yourself! There's a place for everyone!\n");
        fullLogo.append("\nInsert your username:");
        dataOutputStream.writeUTF(fullLogo.toString());
        dataOutputStream.flush();
        String username = dataInputStream.readUTF();
        // Start the login/register
        LoginAndRegister loginService = new LoginAndRegister(this, connection);
        username = loginService.login(username);
        // If user exits login service
        if(username == null){
            startAuctionHouse();
            return;
        }
        // Login/Register successful
        this.username = username;
        Server.loggedInClients.put(this, username);
        Server.loggedInClientsNum++;
        dataOutputStream.writeUTF("\n\nWelcome " +username +" it's a pleasure to have you!\n");
        dataOutputStream.flush();
    }

    void showWallet() throws SQLException, IOException{
        statement = connection.createStatement();
        String getWallet = String.format("SELECT wallet FROM Users WHERE username == '%s';", this.username);
        ResultSet wallet = statement.executeQuery(getWallet);
        wallet.next();
        float money = wallet.getFloat("wallet");
        dataOutputStream.writeUTF("\n"+username +" you currently have" +money +" gold in your pockets.\n");
        dataOutputStream.flush();
    }

    //handle txt reading to display
    public StringBuilder writeASC(String path) throws IOException{
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
        // client login handler
        try{
            startAuctionHouse();
        } catch(IOException | SQLException error) {
            error.printStackTrace();
        }
        Market market = new Market(this);
        while (true){
            try{
                dataOutputStream.writeUTF("\nPlease insert a command (enter 'commands' to see the full list of commands):");

                received = dataInputStream.readUTF();
                if(received.toLowerCase().equals("exit")){
                    endConnection();
                    break;
                }
                
                // write on output stream based on the
                // answer from the client
                switch (received.toLowerCase()){
                    case "commands" :
                        StringBuilder listCommands = writeASC("resources/commands.txt");
                        dataOutputStream.writeUTF(listCommands.toString());
                        break;

                    case "wallet":
                        showWallet();
                        break;

                    case "add item" :
                        market.addItem();
                        break;

                    case "sell item":
                        market.sellItem();
                        break;

                    case "get item" :
                        market.getItem();
                        break;

                    case "list my items" :
                        market.listItems();
                        break;

                    case "list shop":
                        market.listShop();
                        break;

                    case "buy":
                        market.buyItem();
                        break;

                    case "list proposals" :
                        break;

                    case "item X detail" :
                        break;

                    default:
                        dataOutputStream.writeUTF("Invalid input");
                        dataOutputStream.flush();
                        break;
                }
            } catch (IOException | SQLException error) {
                break;
            }
        }

        try{
            // closing resources
            connection.close();
            this.dataInputStream.close();
            this.dataOutputStream.close();
            endConnection();
        } catch(IOException | SQLException error){
            error.printStackTrace();
        }
    }
}