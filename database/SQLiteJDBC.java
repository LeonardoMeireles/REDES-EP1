package database;

import java.sql.*;

public class SQLiteJDBC {
    private Connection connection = null;

    // Connect/Create a database
    public boolean connect(){
        try{
            String url = "jdbc:sqlite:database/AuctionHouse.db";
            this.connection = DriverManager.getConnection(url);
        } catch(SQLException error){
            System.err.println(error.getMessage());
            return false;
        }
        return true;
    }

    public boolean disconnect(){
        try{
            if(this.connection.isClosed() == false){
                this.connection.close();
            }
            String url = "jdbc:sqlite:database/users.db";
            this.connection = DriverManager.getConnection(url);
        } catch(SQLException error){
            System.err.println(error.getMessage());
            return false;
        }
        return true;
    }

}