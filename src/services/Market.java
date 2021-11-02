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
        } finally{
            return null;
        }
    }

    public void getItem() throws IOException {
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
        } catch (IOException | SQLException | NumberFormatException error) {
            client.dataOutputStream.writeUTF("Sorry there buddy something went wrong try again.\n");
            return;
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
        } catch (IOException | SQLException | NumberFormatException error) {
            client.dataOutputStream.writeUTF("Sorry there buddy something went wrong try again.\n");
            return;
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

    public void bargainItem() throws IOException{
        try{
            client.dataOutputStream.writeUTF("What is the shop id for the item you wish bargain for? (type 'list shop' to see all the items up for sale)");
            String shopId = client.dataInputStream.readUTF();
            if(shopId.toLowerCase().equals("cancel")){
                return;
            } else if(shopId.toLowerCase().equals("list shop")){
                listShop();
                bargainItem();
                return;
            } else{
                statement = connection.createStatement();
                String findItemQuery = String.format("SELECT *, Items.owner  FROM Shop INNER JOIN Items ON Items.id = Shop.itemId WHERE Shop.id == '%o';", Integer.parseInt(shopId));
                ResultSet itemFound = statement.executeQuery(findItemQuery);
                if(itemFound.next()){
                    if(itemFound.getString("owner").equals(client.username)){
                        client.dataOutputStream.writeUTF("Sorry but you can only counter-offer to your own items.\n");
                        return;
                    } else{
                        client.dataOutputStream.writeUTF("How much are you offering to buy the item for?");
                        String offer = client.dataInputStream.readUTF();
                        client.dataOutputStream.writeUTF("Would you like to leave a message?\nY: yes\tN: no");
                        String answer = client.dataInputStream.readUTF();
                        String message = null;
                        if(answer.toLowerCase().equals("y") || answer.toLowerCase().equals("yes")){
                            client.dataOutputStream.writeUTF("Please leave a message for the owner:");
                            message = client.dataInputStream.readUTF();
                        }
                        String findBargain = String.format("SELECT * FROM Bargain WHERE buyerUsername == '%s' AND shopId == '%o';", client.username, Integer.parseInt(shopId));
                        ResultSet bargainFound = statement.executeQuery(findBargain);
                        if(bargainFound.next()){
                            String updateOfferQuery = String.format("UPDATE Bargain SET offer = '%.2f' WHERE id == '%o';", Float.parseFloat(offer), bargainFound.getInt("id"));
                            statement.executeUpdate(updateOfferQuery);
                        } else{
                            String newOfferQuery = String.format("INSERT INTO Bargain (shopId, buyerUsername, offer, message) VALUES('%o', '%s', '%.2f', '%s')", Integer.parseInt(shopId), client.username, Float.parseFloat(offer), message);
                            statement.executeUpdate(newOfferQuery);
                        }
                        client.dataOutputStream.writeUTF("Your offer was sent!\n");
                        statement.close();
                    }
                    return;
                } else{
                    client.dataOutputStream.writeUTF("Sorry there buddy but I couldn't find that item in the shop.\n");
                }
            }
        } catch (IOException | SQLException | NumberFormatException error) {
            client.dataOutputStream.writeUTF("Sorry there friend, it appears you wrote the wrong input.\n");
        }
    }

    public void listOffers() throws IOException{
        try {
            statement= connection.createStatement();
            ResultSet offers = statement.executeQuery("SELECT Bargain.id, Items.name, Items.description, Shop.price, Bargain.buyerUsername, Bargain.offer, Bargain.message FROM Bargain INNER JOIN Shop ON Shop.id = Bargain.shopId INNER JOIN Items ON Items.id = Shop.itemId WHERE Items.owner == '" +client.username + "';");
            StringBuilder text = new StringBuilder();
            text.append("Offers: \n\n\n");
            if (offers.isBeforeFirst()){
                while(offers.next()) {
                    int bargainId = offers.getInt("id");
                    String itemName = offers.getString("name");
                    String itemPrice = offers.getString("price");
                    String buyerUsername = offers.getString("buyerUsername");
                    Float offer = offers.getFloat("offer");
                    String message = offers.getString("message");

                    text.append("Id: " + bargainId +"\n");
                    text.append("Item name: " + itemName + " || Price listed: " +itemPrice +"\n\n");
                    text.append("Buyer: "+ buyerUsername + " || Offer: " +offer +" gold\n");
                    text.append("Message: "+ message + "\n");
                    text.append("------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
                }
                client.dataOutputStream.writeUTF(text.toString());
            } else{
                client.dataOutputStream.writeUTF("You haven't received any offers.\n");
            }
            offers.close();
            statement.close();
        } catch (IOException | SQLException | NumberFormatException error) {
            client.dataOutputStream.writeUTF("Sorry there buddy something went wrong try again.\n");
            return;
        }
    }

    public void counterOffer() throws IOException{
        try{
            client.dataOutputStream.writeUTF("What is the offer's id you wish to counteroffer? (type 'list offers' to see all the items up for sale)");
            String offerId = client.dataInputStream.readUTF();
            if(offerId.toLowerCase().equals("cancel")){
                return;
            } else if(offerId.toLowerCase().equals("list offers")){
                listOffers();
                counterOffer();
                return;
            } else{
                statement = connection.createStatement();
                String findOfferQuery = String.format("SELECT Bargain.id, Items.owner FROM Bargain INNER JOIN Shop ON Shop.id = Bargain.shopId INNER JOIN Items ON Items.id = Shop.itemId WHERE Bargain.id == '%s' AND Items.owner == '%s';", Integer.parseInt(offerId), client.username);
                ResultSet offerFound = statement.executeQuery(findOfferQuery);
                if(offerFound.next()){
                    int offerFoundId = offerFound.getInt("id");
                    client.dataOutputStream.writeUTF("What is your counteroffer?");
                    String counterOffer = client.dataInputStream.readUTF();
                    client.dataOutputStream.writeUTF("Would you like to leave a message?\nY: yes\tN: no");
                    String answer = client.dataInputStream.readUTF();
                    String message = null;
                    if(answer.toLowerCase().equals("y") || answer.toLowerCase().equals("yes")){
                        client.dataOutputStream.writeUTF("Please leave a message for the buyer:");
                        message = client.dataInputStream.readUTF();
                    }
                    String findCounterOfferQuery = String.format("SELECT * FROM CounterOffer WHERE bargainId == '%o';", offerFoundId);
                    ResultSet counterOfferFound = statement.executeQuery(findCounterOfferQuery);
                    if(counterOfferFound.next()){
                        String updateCounterOfferQuery = String.format("UPDATE CounterOffer SET counterOffer = '%.2f' WHERE id == '%o';", Float.parseFloat(counterOffer), counterOfferFound.getInt("id"));
                        statement.executeUpdate(updateCounterOfferQuery);
                    } else{
                        String newCounterOfferQuery = String.format("INSERT INTO CounterOffer (bargainId, counterOffer, message) VALUES('%o', '%.2f', '%s')", offerFoundId, Float.parseFloat(counterOffer), message);
                        statement.executeUpdate(newCounterOfferQuery);
                    }
                    client.dataOutputStream.writeUTF("Your counteroffer was sent!\n");
                    statement.close();
                } else{
                    client.dataOutputStream.writeUTF("Sorry there buddy but that id wasn't valid.\n");
                }
            }
            statement.close();
        } catch (IOException | SQLException | NumberFormatException error) {
            client.dataOutputStream.writeUTF("Sorry there buddy something went wrong try again.\n");
            return;
        }
    }

}
