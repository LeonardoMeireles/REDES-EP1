package database;

import java.sql.*;

public class SQLiteJDBC {

    // Connect/Create a database
    public Connection connect(){
        Connection newConnection;
        try{
            String url = "jdbc:sqlite:database/AuctionHouse.db";
            newConnection = DriverManager.getConnection(url);
        } catch(SQLException error){
            System.err.println(error.getMessage());
            return null;
        }
        return(newConnection);
    }

    public boolean disconnect(Connection connection){
        try{
            if(connection.isClosed() == false){
                connection.close();
            }
        } catch(SQLException error){
            System.err.println(error.getMessage());
            return false;
        }
        return true;
    }

}