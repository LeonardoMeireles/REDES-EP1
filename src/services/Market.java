package services;

import connection.ClientHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class Market {

    ClientHandler client;
    Statement statement;
    Connection connection;

    public Market(ClientHandler client){
        this.client = client;
        this.connection = client.dbConnection;
    }

    public void addItem() throws IOException{
        Date date = new Date();
        String name = "";
        String price = "";
        String description = "";
        try{
            client.dataOutputStream.writeUTF("Product name, please keep under 50 characters (Type 'Cancel' to exit):");
            client.dataOutputStream.flush();
            name = client.dataInputStream.readUTF();
            while(name.length() > 140 || name.equals("Cancel")){
                client.dataOutputStream.writeUTF("Please write a product name under 50 characters (Type 'Cancel' to exit):");
            }
            client.dataOutputStream.writeUTF("Price (Type 'Cancel' to exit):");
            client.dataOutputStream.flush();
            price = client.dataInputStream.readUTF();
            client.dataOutputStream.writeUTF("Description, please keep under 140 characters (Type 'Cancel' to exit):");
            client.dataOutputStream.flush();
            description = client.dataInputStream.readUTF();
            while(description.length() > 140 || description.equals("Cancel")){
                client.dataOutputStream.writeUTF("Please write a description name under 140 characters (Type 'Cancel' to exit):");
            }
            statement = connection.createStatement();
            String newItemQuery = String.format("INSERT INTO Items (name, price, description, owner) VALUES('%s','%f', '%s', '%s')", name, Float.parseFloat(price), description, client.username);
            statement.executeUpdate(newItemQuery);
            return;
        } catch (IOException | SQLException error) {
            error.printStackTrace();
        }
    }

    public void listShop() throws IOException{
        try {
            statement= connection.createStatement();
            String listItems = String.format("SELECT * FROM Items;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String listItems;

    }

}