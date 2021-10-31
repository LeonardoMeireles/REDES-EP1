package services;

import connection.ClientHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Locale;

public class Market {

    ClientHandler client;
    Statement statement;
    Connection connection;

    public Market(ClientHandler client) {
        this.client = client;
        this.connection = client.dbConnection;
    }

    public void addItem() throws IOException {
        Date date = new Date();
        String name = "";
        String price = "";
        String description = "";
        try{
            client.dataOutputStream.writeUTF("\nProduct name, please keep under 50 characters (Type 'Cancel' to exit):");
            client.dataOutputStream.flush();
            name = client.dataInputStream.readUTF();
            while(name.length() > 140){
                client.dataOutputStream.writeUTF("Please write a product name under 50 characters (Type 'Cancel' to exit):");
                client.dataOutputStream.flush();
                name = client.dataInputStream.readUTF();
                if(name.toLowerCase().equals("cancel")){
                    return;
                }
            }
            client.dataOutputStream.writeUTF("Price (Type 'Cancel' to exit):");
            client.dataOutputStream.flush();
            price = client.dataInputStream.readUTF();
            client.dataOutputStream.writeUTF("Description, please keep under 140 characters (Type 'Cancel' to exit):");
            client.dataOutputStream.flush();
            description = client.dataInputStream.readUTF();
            while(description.length() > 140){
                client.dataOutputStream.writeUTF("Please write a description name under 140 characters (Type 'Cancel' to exit):");
                client.dataOutputStream.flush();
                description = client.dataInputStream.readUTF();
                if(description.toLowerCase().equals("cancel")){
                    return;
                }
            }
            statement = connection.createStatement();
            String newItemQuery = String.format("INSERT INTO Items (name, price, description, owner) VALUES('%s','%.2f', '%s', '%s')", name, Float.parseFloat(price), description, client.username);
            client.dataOutputStream.writeUTF("Item successfully added, I'll be sure to keep it safe and sound!\n");
            statement.executeUpdate(newItemQuery);
            return;
        } catch (IOException | SQLException error) {
            error.printStackTrace();
        }
    }

    public void getItem() throws IOException {
        try{
            client.dataOutputStream.writeUTF("What is the id of the item you wish to have back traveler? (type 'List Items' to see all your available items)");
            String itemId = client.dataInputStream.readUTF();
            if(itemId.toLowerCase().equals("Cancel")){
                return;
            } else if(itemId.toLowerCase().equals("list items")){
                // Still need to make this function
            } else{
                statement = connection.createStatement();
                String findItemQuery = String.format("SELECT * FROM Items WHERE id == '%o';", Integer.parseInt(itemId));
                ResultSet itemFound = statement.executeQuery(findItemQuery);
                if(itemFound.next()){
                    if(itemFound.getString("owner").equals(client.username)){
                        String deleteItemQuery = String.format("DELETE FROM Items WHERE id == '%o';", Integer.parseInt(itemId));
                        statement.executeUpdate(deleteItemQuery);
                        statement.close();
                        client.dataOutputStream.writeUTF("Here is your item friend.\n");
                    } else{
                        client.dataOutputStream.writeUTF("Sorry there buddy but I couldn't find that item in your stash.\n");
                    }
                    return;
                } else{
                    client.dataOutputStream.writeUTF("Sorry there buddy but I couldn't find that item in your stash.\n");
                }
            }
        } catch (IOException | SQLException error) {
            error.printStackTrace();
        }
    }

    public void listShop() throws IOException{
        try {
            statement= connection.createStatement();
            String listItems = String.format("SELECT * FROM Items WHERE owner == '%s';",client.username);
            ResultSet items = statement.executeQuery(listItems);
            boolean haveItem = false;

            StringBuilder text = new StringBuilder();
            text.append("YOUR SHOP: \n\n\n");
            while(items.next()) {
                haveItem = true;

                String name = items.getString("name");
                float price = items.getFloat("price");
                String description = items.getString("description");
                String owner = items.getString("owner");

                text.append("Name: " + name + " || ");
                text.append("Price: " + price + "\n");
                text.append("Description:\n"+ description + "\n");
                text.append("Owner: " + owner + "\n");
                text.append("----------------------------------------------------------------------------------------\n");
            }
            if(haveItem == false){
                client.dataOutputStream.writeUTF("You don't have any item in Auction");
            } else{
                client.dataOutputStream.writeUTF(text.toString());
            }
            items.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        String listItems;

    }

}
