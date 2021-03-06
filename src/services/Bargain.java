package services;

import connection.ClientHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Bargain {

    Market market;
    ClientHandler client;
    Statement statement;
    Connection connection;

    public Bargain(Market market){
        this.market = market;
        this.client = market.client;
        this.connection = market.connection;
    }

    public void bargainItem() throws IOException {
        try{
            client.dataOutputStream.writeUTF("What is the shop id for the item you wish bargain for? (type 'list shop' to see all the items up for sale)");
            String shopId = client.dataInputStream.readUTF();
            if(shopId.toLowerCase().equals("cancel")){
                return;
            } else if(shopId.toLowerCase().equals("list shop")){
                market.listShop();
                bargainItem();
                return;
            } else{
                statement = connection.createStatement();
                String findItemQuery = String.format("SELECT *, Items.owner  FROM Shop INNER JOIN Items ON Items.id = Shop.itemId WHERE Shop.id == '%o';", Integer.parseInt(shopId));
                ResultSet itemFound = statement.executeQuery(findItemQuery);
                if(itemFound.next()){
                    if(itemFound.getString("owner").equals(client.username)){
                        client.dataOutputStream.writeUTF("Sorry but you can only make counteroffers to your own items.\n");
                        return;
                    } else if(itemFound.getInt("bargain") == 0){
                        client.dataOutputStream.writeUTF("Sorry but you can't make offers for that item.\n");
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
            ResultSet offers = statement.executeQuery("SELECT Bargain.id, Items.name, Shop.price, Bargain.buyerUsername, Bargain.offer, Bargain.message FROM Bargain INNER JOIN Shop ON Shop.id = Bargain.shopId INNER JOIN Items ON Items.id = Shop.itemId WHERE Items.owner == '" +client.username + "';");
            StringBuilder textOffers = new StringBuilder();
            textOffers.append("Offers: \n\n\n");
            if (offers.isBeforeFirst()){
                while(offers.next()) {
                    int bargainId = offers.getInt("id");
                    String itemName = offers.getString("name");
                    String itemPrice = offers.getString("price");
                    String buyerUsername = offers.getString("buyerUsername");
                    Float offer = offers.getFloat("offer");
                    String message = offers.getString("message");

                    textOffers.append("Id: " + bargainId +"\n");
                    textOffers.append("Item name: " + itemName + " || Price listed: " +itemPrice +"\n\n");
                    textOffers.append("Buyer: "+ buyerUsername + " || Offer: " +offer +" gold\n");
                    textOffers.append("Message: "+ message + "\n");
                    textOffers.append("------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
                }
                client.dataOutputStream.writeUTF(textOffers.toString());
            } else{
                client.dataOutputStream.writeUTF("You haven't received any offers.\n");
            }
            ResultSet counterOffers = statement.executeQuery("SELECT CounterOffer.id, Bargain.buyerUsername, Items.name, Shop.price, Items.owner, Bargain.offer, CounterOffer.counterOffer,  CounterOffer.message FROM CounterOffer INNER JOIN Bargain ON Bargain.id = CounterOffer.bargainId INNER JOIN Shop ON Shop.id = Bargain.shopId INNER JOIN Items ON Items.id = Shop.itemId WHERE Bargain.buyerUsername == '" +client.username + "';");
            StringBuilder textCounterOffers = new StringBuilder();
            textCounterOffers.append("\nCounteroffers: \n\n\n");
            if (counterOffers.isBeforeFirst()){
                while(counterOffers.next()) {
                    int counterOfferId = offers.getInt("id");
                    String itemName = offers.getString("name");
                    String itemPrice = offers.getString("price");
                    String ownerUsername = offers.getString("owner");
                    Float offer = offers.getFloat("offer");
                    Float counterOffer = offers.getFloat("counterOffer");
                    String message = offers.getString("message");

                    textCounterOffers.append("Id: " + counterOfferId +"\n");
                    textCounterOffers.append("Item name: " + itemName + " || Price listed: " +itemPrice +"\n\n");
                    textCounterOffers.append("Your offer: " +offer +" gold\n");
                    textCounterOffers.append("Owner: "+ ownerUsername + " || Counteroffer: " +counterOffer +" gold\n");
                    textCounterOffers.append("Message: "+ message + "\n");
                    textCounterOffers.append("------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
                }
                client.dataOutputStream.writeUTF(textCounterOffers.toString());
            } else{
                client.dataOutputStream.writeUTF("You haven't received any counteroffers.\n");
            }
            offers.close();
            statement.close();
        } catch (IOException | SQLException | NumberFormatException error) {
            System.out.println(error);
            client.dataOutputStream.writeUTF("Sorry there buddy something went wrong try again.\n");
            return;
        }
    }

    public void counterOffer() throws IOException {
        try {
            client.dataOutputStream.writeUTF("What is the offer's id you wish to counteroffer? (type 'list offers' to see all offers and counteroffers you received)");
            String offerId = client.dataInputStream.readUTF();
            if (offerId.toLowerCase().equals("cancel")) {
                return;
            } else if (offerId.toLowerCase().equals("list offers")) {
                listOffers();
                counterOffer();
                return;
            } else {
                statement = connection.createStatement();
                String findOfferQuery = String.format("SELECT Bargain.id, Items.owner FROM Bargain INNER JOIN Shop ON Shop.id = Bargain.shopId INNER JOIN Items ON Items.id = Shop.itemId WHERE Bargain.id == '%s' AND Items.owner == '%s';", Integer.parseInt(offerId), client.username);
                ResultSet offerFound = statement.executeQuery(findOfferQuery);
                if (offerFound.next()) {
                    int offerFoundId = offerFound.getInt("id");
                    client.dataOutputStream.writeUTF("What is your counteroffer?");
                    String counterOffer = client.dataInputStream.readUTF();
                    client.dataOutputStream.writeUTF("Would you like to leave a message?\nY: yes\tN: no");
                    String answer = client.dataInputStream.readUTF();
                    String message = null;
                    if (answer.toLowerCase().equals("y") || answer.toLowerCase().equals("yes")) {
                        client.dataOutputStream.writeUTF("Please leave a message for the buyer:");
                        message = client.dataInputStream.readUTF();
                    }
                    String findCounterOfferQuery = String.format("SELECT * FROM CounterOffer WHERE bargainId == '%o';", offerFoundId);
                    ResultSet counterOfferFound = statement.executeQuery(findCounterOfferQuery);
                    if (counterOfferFound.next()) {
                        String updateCounterOfferQuery = String.format("UPDATE CounterOffer SET counterOffer = '%.2f' WHERE id == '%o';", Float.parseFloat(counterOffer), counterOfferFound.getInt("id"));
                        statement.executeUpdate(updateCounterOfferQuery);
                    } else {
                        String newCounterOfferQuery = String.format("INSERT INTO CounterOffer (bargainId, counterOffer, message) VALUES('%o', '%.2f', '%s')", offerFoundId, Float.parseFloat(counterOffer), message);
                        statement.executeUpdate(newCounterOfferQuery);
                    }
                    client.dataOutputStream.writeUTF("Your counteroffer was sent!\n");
                    statement.close();
                } else {
                    client.dataOutputStream.writeUTF("Sorry there buddy but that id wasn't valid.\n");
                }
            }
            statement.close();
        } catch (IOException | SQLException | NumberFormatException error) {
            client.dataOutputStream.writeUTF("Sorry there buddy something went wrong try again.\n");
            return;
        }
    }

    public void acceptOffer() throws IOException{
        try{
            statement = connection.createStatement();
            client.dataOutputStream.writeUTF("What is the id of the offer you wish to accept adventurer? (type 'list offers' to see all offers and counteroffers you received)");
            String bargainId = client.dataInputStream.readUTF();// shop id that client wants to buy at the shop

            if(bargainId.toLowerCase().equals("cancel")){
                return;
            } else if(bargainId.toLowerCase().equals("list offers")){
                listOffers();
                acceptOffer();
                return;
            } else{ // passed item shopId
                statement = connection.createStatement();
                ResultSet bargainFound = statement.executeQuery(String.format("SELECT Bargain.offer, Bargain.shopId, Bargain.buyerUsername, Items.owner, Shop.itemId FROM Bargain INNER JOIN Shop ON Shop.id = Bargain.shopId INNER JOIN Items ON Items.id = Shop.itemId WHERE Bargain.id == '%o'", Integer.parseInt(bargainId)));// item at shop
                if(bargainFound.next()){
                    if(!bargainFound.getString("owner").equals(client.username)){
                        client.dataOutputStream.writeUTF("You are not the owner of this item");
                        return;
                    } else{
                        String buyerUsername = bargainFound.getString("buyerUsername");
                        Float bargainOffer = bargainFound.getFloat("offer");
                        int shopId = bargainFound.getInt("shopId");
                        int itemId = bargainFound.getInt("itemId");
                        // pay for item / update wallets
                        ResultSet walletOwner = statement.executeQuery(String.format("SELECT wallet FROM Users WHERE username == '%s'", client.username));// item at shop
                        ResultSet walletBuyer = statement.executeQuery(String.format("SELECT wallet FROM Users WHERE username == '%s'", buyerUsername));// item at shop

                        float walletOwnerNew = walletOwner.getFloat("wallet") + bargainOffer;
                        float walletBuyerNew = walletBuyer.getFloat("wallet") - bargainOffer;

                        String updateOwnerWallet = String.format("UPDATE Users SET wallet = '%f' WHERE username == '%s';", walletOwnerNew, client.username);
                        String updateBuyerWallet = String.format("UPDATE Users SET wallet = '%f' WHERE username == '%s';", walletBuyerNew, buyerUsername);
                        statement.executeUpdate(updateOwnerWallet);
                        statement.executeUpdate(updateBuyerWallet);

                        // delete the item from the shop
                        String deleteQueryShop = String.format("DELETE FROM Shop WHERE id = %o;", shopId);
                        statement.executeUpdate(deleteQueryShop);

                        // delete from bargain
                        String deleteBargainQuery = String.format("DELETE FROM Bargain WHERE id = %o;", Integer.parseInt(bargainId));
                        statement.executeUpdate(deleteBargainQuery);

                        // update the item owner
                        String addToInventory = String.format("UPDATE Items SET owner = '%s' WHERE id = %d;", client.username, itemId);
                        statement.executeUpdate(addToInventory);

                        client.dataOutputStream.writeUTF("You sold the item");
                    }
                    statement.close();
                } else{ //didn't find bargain with the ID passed
                    client.dataOutputStream.writeUTF("Sorry there buddy but I couldn't find that offer.\n");
                }
            }
        } catch (IOException | SQLException | NumberFormatException error) {
            client.dataOutputStream.writeUTF("Sorry there buddy but the magical realms seem to be interfering with my shop, please try again.\n");
            return;
        }
    }

    public void acceptCounterOffer() throws IOException{
        try{
            statement = connection.createStatement();
            client.dataOutputStream.writeUTF("What is the id of the counteroffer you wish to accept adventurer? (type 'list offers' to see all the items up for sale)");
            String counterOfferId = client.dataInputStream.readUTF();// shop id that client wants to buy at the shop

            if(counterOfferId.toLowerCase().equals("cancel")){
                return;
            } else if(counterOfferId.toLowerCase().equals("list offers")){
                listOffers();
                acceptCounterOffer();
                return;
            } else{ // passed counterOffer id
                statement = connection.createStatement();
                ResultSet counterOfferFound = statement.executeQuery(String.format("SELECT CounterOffer.counterOffer, CounterOffer.bargainId, Bargain.shopId, Bargain.buyerUsername, Items.owner, Shop.itemId FROM CounterOffer INNER JOIN Bargain ON Bargain.id = CounterOffer.bargainId INNER JOIN Shop ON Shop.id = Bargain.shopId INNER JOIN Items ON Items.id = Shop.itemId WHERE CounterOffer.id == '%o'", Integer.parseInt(counterOfferId)));// item at shop
                if(counterOfferFound.next()){
                    if(!counterOfferFound.getString("buyerUsername").equals(client.username)){
                        client.dataOutputStream.writeUTF("You did not make that offer");
                        return;
                    } else{
                        String ownerUsername = counterOfferFound.getString("owner");
                        Float counterOffer = counterOfferFound.getFloat("counterOffer");
                        int shopId = counterOfferFound.getInt("shopId");
                        int itemId = counterOfferFound.getInt("itemId");
                        int bargainId = counterOfferFound.getInt("bargainId");
                        // pay for item / update wallets
                        ResultSet walletOwner = statement.executeQuery(String.format("SELECT wallet FROM Users WHERE username == '%s'", ownerUsername));// item at shop
                        ResultSet walletBuyer = statement.executeQuery(String.format("SELECT wallet FROM Users WHERE username == '%s'", client.username));// item at shop

                        float walletOwnerNew = walletOwner.getFloat("wallet") + counterOffer;
                        float walletBuyerNew = walletBuyer.getFloat("wallet") - counterOffer;

                        String updateOwnerWallet = String.format("UPDATE Users SET wallet = '%f' WHERE username == '%s';", walletOwnerNew, ownerUsername);
                        String updateBuyerWallet = String.format("UPDATE Users SET wallet = '%f' WHERE username == '%s';", walletBuyerNew, client.username);
                        statement.executeUpdate(updateOwnerWallet);
                        statement.executeUpdate(updateBuyerWallet);

                        // delete the item from the shop
                        String deleteQueryShop = String.format("DELETE FROM Shop WHERE id = %o;", shopId);
                        statement.executeUpdate(deleteQueryShop);

                        // delete from bargain
                        String deleteBargainQuery = String.format("DELETE FROM Bargain WHERE id = %o;", bargainId);
                        statement.executeUpdate(deleteBargainQuery);

                        // delete from counteroffer
                        String deleteCounterOfferQuery = String.format("DELETE FROM CounterOffer WHERE id = %o;", Integer.parseInt(counterOfferId));
                        statement.executeUpdate(deleteBargainQuery);

                        // update the item owner
                        String addToInventory = String.format("UPDATE Items SET owner = '%s' WHERE id = %d;", client.username, itemId);
                        statement.executeUpdate(addToInventory);

                        client.dataOutputStream.writeUTF("You bought the item");
                    }
                    statement.close();
                } else{ //didn't find bargain with the ID passed
                    client.dataOutputStream.writeUTF("Sorry there buddy but I couldn't find that counteroffer.\n");
                }
            }
        } catch (IOException | SQLException | NumberFormatException error) {
            error.printStackTrace();
            return;
        }
    }


}
