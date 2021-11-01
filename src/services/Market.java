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
        this.connection = client.connection;
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
            String newItemQuery = String.format("INSERT INTO Items (name, description, owner) VALUES('%s', '%s', '%s')", name, description, client.username);
            client.dataOutputStream.writeUTF("Item successfully added, I'll be sure to keep it safe and sound!\n");
            statement.executeUpdate(newItemQuery);
            return;
        } catch (IOException | SQLException error) {
            error.printStackTrace();
        }
    }

    public void sellItem() throws IOException{
        try{
            client.dataOutputStream.writeUTF("\nSo you are looking to sell, what's the item's id that you are looking to put on sale?\n(To list your items type 'list my items' or 'add item' to add an item to your stash)");
            client.dataOutputStream.flush();
            String itemId = client.dataInputStream.readUTF();
            if(itemId.toLowerCase().equals("list my items")){
                listItems();
                sellItem();
                return;
            } else if(itemId.toLowerCase().equals("add item")){
                addItem();
                sellItem();
                return;
            } else{
                statement = connection.createStatement();
                ResultSet itemFound = getFromStash(itemId);
                if(itemFound.next() == true){
                    client.dataOutputStream.writeUTF("For how much are you trying to sell this for?");
                    String price = client.dataInputStream.readUTF();
                    client.dataOutputStream.writeUTF("Are you willing to bargain for it?\nY: yes\tN: no");
                    String bargainString = client.dataInputStream.readUTF();
                    int bargain = 0;
                    if(bargainString.toLowerCase().equals("y") || bargainString.toLowerCase().equals("yes")) {
                        bargain = 1;
                    }
                    String newItemQuery = String.format("INSERT INTO Shop (itemId, price, bargain) VALUES('%o', '%.2f', '%o')", Integer.parseInt(itemId), Float.parseFloat(price), bargain);
                    statement.executeUpdate(newItemQuery);
                    client.dataOutputStream.writeUTF("Your item is now up for sale in the shop!\n");
                    statement.close();
                } else{
                    client.dataOutputStream.writeUTF("Sorry there buddy but I couldn't find that item in your stash.\n");
                }
                statement.close();
            }
        } catch(IOException | SQLException error) {
            error.printStackTrace();
            client.dataOutputStream.writeUTF("Something went wrong my dear friend, please try again.\n");
            return;
        }
    }

    public ResultSet getFromStash(String itemId) throws SQLException, IOException {
        statement = connection.createStatement();
        String findItemQuery = String.format("SELECT * FROM Items WHERE id == '%o';", Integer.parseInt(itemId));
        ResultSet itemFound = statement.executeQuery(findItemQuery);
        return itemFound;
    }

    public void getItem() throws IOException {
        try{
            client.dataOutputStream.writeUTF("What is the id of the item you wish to have back traveler? (type 'List Items' to see all your available items)");
            String itemId = client.dataInputStream.readUTF();
            if(itemId.toLowerCase().equals("Cancel")){
                return;
            } else if(itemId.toLowerCase().equals("list items")){
                listItems();
                getItem();
                return;
            } else{
                ResultSet itemFound = getFromStash(itemId);
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

    public void listItems() throws IOException{
        try {
            statement= connection.createStatement();
            String listItems = String.format("SELECT * FROM Items WHERE owner == '%s';",client.username);
            ResultSet items = statement.executeQuery(listItems);

            StringBuilder text = new StringBuilder();
            text.append("YOUR SHOP: \n\n\n");
            if (items.isBeforeFirst() ) {
                while(items.next()) {
                    int id = items.getInt("id");
                    String name = items.getString("name");
                    String description = items.getString("description");

                    text.append("ID: " + id + " || ");
                    text.append("Name: " + name + "\n");
                    text.append("Description: "+ description + "\n");
                    text.append("------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
                }
                client.dataOutputStream.writeUTF(text.toString());
            } else{
                client.dataOutputStream.writeUTF("You don't have any item in Auction");
            }
            items.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void listShop() throws IOException{
        try {
            statement= connection.createStatement();
            ResultSet items = statement.executeQuery("SELECT Shop.id, Items.name, Items.description, Items.owner, Shop.price, Shop.bargain FROM Shop INNER JOIN Items ON Shop.itemId = Items.id;");
            StringBuilder text = new StringBuilder();
            text.append("SHOP: \n\n\n");
            if (items.isBeforeFirst()){
                while(items.next()) {
                    int shopId = items.getInt("bargain");
                    String name = items.getString("name");
                    String owner = items.getString("owner");
                    String description = items.getString("description");
                    float price = items.getFloat("price");
                    int bargain = items.getInt("bargain");
                    String bargainable = "False";
                    if(bargain == 1){
                        bargainable = "True";
                    }
                    text.append("Id: " + shopId +"\n");
                    text.append("Name: " + name + " || Owner: " +owner +"\n");
                    text.append("Description: "+ description + "\n");
                    text.append("Price: "+ price + "\n");
                    text.append("Open to bargain: "+ bargainable + "\n");
                    text.append("------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
                }
                client.dataOutputStream.writeUTF(text.toString());
            } else{
                client.dataOutputStream.writeUTF("There are no items in the shop right now.\n");
            }
            items.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void buyItem() throws IOException{
        try{
            client.dataOutputStream.writeUTF("What is the id of the item you wish to buy adventurer? (type 'List Shop' to see all the items up for sale)");
            String itemId = client.dataInputStream.readUTF();
            if(itemId.toLowerCase().equals("Cancel")){
                return;
            } else if(itemId.toLowerCase().equals("list shop")){
                listShop();
                buyItem();
                return;
            } else{
                ResultSet itemFound = getFromStash(itemId);
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

}
