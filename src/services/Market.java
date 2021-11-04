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
    public Bargain bargain;

    public Market(ClientHandler client) {
        this.client = client;
        this.connection = client.connection;
        this.bargain = new Bargain(this);
    }

    public void addItem() throws IOException { //add item to the market
        Date date = new Date();
        String name = "";
        String price = "";
        String description = "";
        try{
            client.dataOutputStream.writeUTF("\nProduct name, please keep under 50 characters (Type 'cancel' to exit):");
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
            client.dataOutputStream.writeUTF("Description, please keep under 140 characters (Type 'cancel' to exit):");
            client.dataOutputStream.flush();
            description = client.dataInputStream.readUTF();
            while(description.length() > 140){
                client.dataOutputStream.writeUTF("Please write a description name under 140 characters (Type 'cancel' to exit):");
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
        } catch (IOException | SQLException | NumberFormatException error) {
            client.dataOutputStream.writeUTF("Sorry there buddy something went wrong try again.\n");
            return;
        }
    }

    public void sellItem() throws IOException{ // add item to sell
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
                if(itemFound.next()){
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
                } else{
                    client.dataOutputStream.writeUTF("Sorry there buddy but I couldn't find that item in your stash.\n");
                }
                statement.close();
            }
        } catch(IOException | SQLException | NullPointerException error) {
            error.printStackTrace();
            client.dataOutputStream.writeUTF("Something went wrong my dear friend, please try again.\n");
            sellItem();
            return;
        }
    }

    public ResultSet getFromStash(String itemId) throws SQLException, IOException {
        try{
            statement = connection.createStatement();
            String findItemQuery = String.format("SELECT * FROM Items WHERE id == '%o';", Integer.parseInt(itemId));
            ResultSet itemFound = statement.executeQuery(findItemQuery);
            return itemFound;
        } catch(SQLException | NumberFormatException error){
            return null;
        }
    }

    public ResultSet getFromShop(String itemId) throws SQLException, IOException { // search for items at shop list by shop ID
        statement = connection.createStatement();
        String findItemQuery = String.format("SELECT * FROM Shop WHERE id == '%o';", Integer.parseInt(itemId));
        ResultSet itemFound = statement.executeQuery(findItemQuery);
        return itemFound;
    }

    public float getWallet(String user) throws SQLException, IOException { // search for items at shop list by shop ID
        statement = connection.createStatement();
        String findUser = String.format("SELECT wallet FROM Users WHERE username == '%s';", user);
        ResultSet theUser = statement.executeQuery(findUser);
        float walletValue = theUser.getFloat("wallet");
        return walletValue;
    }

    public void getItem() throws IOException { // get back the item at the market
        try{
            client.dataOutputStream.writeUTF("What is item's id you wish to have back traveler? (type 'list items' to see all your available items)");
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
                        client.dataOutputStream.writeUTF("Here is your item friend.\n");
                    } else{
                        client.dataOutputStream.writeUTF("Sorry there buddy but I couldn't find that item in your stash.\n");
                    }
                    statement.close();
                    return;
                } else{
                    client.dataOutputStream.writeUTF("Sorry there buddy but I couldn't find that item in your stash.\n");
                }
            }
        } catch (IOException | SQLException | NumberFormatException error) {
            client.dataOutputStream.writeUTF("Sorry there buddy something went wrong try again.\n");
            return;
        }
    }

    public void listItems() throws IOException{ // list items at your shop
        try {
            statement= connection.createStatement();
            String listItems = String.format("SELECT * FROM Items WHERE owner == '%s';",client.username);
            ResultSet items = statement.executeQuery(listItems);

            StringBuilder text = new StringBuilder();
            text.append("YOUR ITEMS: \n\n\n");
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
        } catch (IOException | SQLException | NumberFormatException error) {
            client.dataOutputStream.writeUTF("Sorry there buddy something went wrong try again.\n");
            return;
        }
    }

    public void listShop() throws IOException{ // list items in the shop
        try {
            statement= connection.createStatement();
            ResultSet items = statement.executeQuery("SELECT Shop.id, Items.name, Items.description, Items.owner, Shop.price, Shop.bargain FROM Shop INNER JOIN Items ON Shop.itemId = Items.id;");
            StringBuilder text = new StringBuilder();
            text.append("SHOP: \n\n\n");
            if (items.isBeforeFirst()){
                while(items.next()) {
                    int shopId = items.getInt("id");
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
        } catch (IOException | SQLException | NumberFormatException error) {
            client.dataOutputStream.writeUTF("Sorry there buddy something went wrong try again.\n");
            return;
        }
    }

    public void buyItem() throws IOException{ // buy item at the shop
        try{
            statement = connection.createStatement();
            client.dataOutputStream.writeUTF("What is the id of the item you wish to buy adventurer? (type 'List Shop' to see all the items up for sale)");
            String shopItemId = client.dataInputStream.readUTF();// shop id that client wants to buy at the shop

            if(shopItemId.toLowerCase().equals("cancel")){
                return;
            } else if(shopItemId.toLowerCase().equals("list shop")){
                listShop();
                buyItem();
            } else{ // passed item shopId
                ResultSet itemFoundShop = getFromShop(shopItemId);// item at shop
                if(itemFoundShop.next()){
                    ResultSet itemItemList = getFromStash(itemFoundShop.getString("itemId")); // item at item list that client wants to buy at the shop

                    if(itemItemList.getString("owner").equals(client.username)){ // tried to buy client own item
                        client.dataOutputStream.writeUTF("You are the owner of this item");
                        return;
                    } else{
                        // pay for item / update wallets
                        float walletOwnerNew = getWallet(itemItemList.getString("owner")) + itemFoundShop.getFloat("price");
                        float walletBuyerNew = getWallet(client.username) - itemFoundShop.getFloat("price");

                        String getOwnerUsername = String.format("SELECT Users.username FROM Users INNER JOIN Items ON Items.owner = Users.username INNER JOIN Shop ON Shop.itemId = Items.id WHERE Items.id == '%o';", Integer.parseInt(shopItemId));
                        ResultSet foundOwner = statement.executeQuery(getOwnerUsername);
                        String ownerUsername = null;

                        ownerUsername = foundOwner.getString("username");

                        String updateOwnerWallet = String.format("UPDATE Users SET wallet = '%f' WHERE username == '%s';", walletOwnerNew, ownerUsername);
                        String updateBuyerWallet = String.format("UPDATE Users SET wallet = '%f' WHERE username == '%s';", walletBuyerNew, client.username);
                        statement.executeUpdate(updateOwnerWallet);
                        statement.executeUpdate(updateBuyerWallet);

                        // delete the item from the shop
                        String deleteQueryShop = String.format("DELETE FROM Shop WHERE id = %o;", Integer.parseInt(shopItemId));
                        statement.executeUpdate(deleteQueryShop);

                        // update the item owner
                        String addToInventory = String.format("UPDATE Items SET owner = '%s' WHERE id = %d;", client.username, itemFoundShop.getInt("itemId"));
                        statement.executeUpdate(addToInventory);

                        client.dataOutputStream.writeUTF("You bought the item");
                    }
                    itemFoundShop.close();
                    itemItemList.close();
                } else{ //didn't find item with the ID passed
                    client.dataOutputStream.writeUTF("Sorry there buddy but I couldn't find that item in stash.\n");
                }
                statement.close();
            }
        } catch (IOException | SQLException | NumberFormatException error) {
            error.printStackTrace();
        }
    }

}
