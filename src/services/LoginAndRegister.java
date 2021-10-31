package services;

import connection.ClientHandler;
import database.SQLiteJDBC;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LoginAndRegister{

    ClientHandler clientHandler;
    Connection connection;
    Statement statement = null;

    public LoginAndRegister(ClientHandler clientHandler, Connection connection){
        this.clientHandler = clientHandler;
        this.connection = connection;
    }

    public String login(String username) throws SQLException, IOException{
        //search for username and create new account if username is not found
        statement = connection.createStatement();
        String findUserQuery = String.format("SELECT * FROM Users WHERE username == '%s';", username);
        ResultSet userFound = statement.executeQuery(findUserQuery);
        // If user was found, login user
        if(userFound.next()){
            while(true){
                clientHandler.dataOutputStream.writeUTF("Insert your password:");
                clientHandler.dataOutputStream.flush();
                String password = clientHandler.dataInputStream.readUTF();
                if(userFound.getString("password").equals(password)){
                    statement.close();
                    return username;
                } else {
                    clientHandler.dataOutputStream.writeUTF("That was the wrong password, would you like to try again?\nY: yes\tN: no");
                    String answer = clientHandler.dataInputStream.readUTF();
                    if(answer.toLowerCase().equals("y")){
                        continue;
                    } else{
                        statement.close();
                        return null;
                    }
                }
            }
        } else { // No user found
            clientHandler.dataOutputStream.writeUTF("\nHuum, i don't know any adventurer with that name, would you like to sign up for my services?\nY: yes\tN: no");
            clientHandler.dataOutputStream.flush();
            while(true){
                statement.close();
                String answer = clientHandler.dataInputStream.readUTF();
                if(answer.toLowerCase().equals("y")){
                    return createAccount();
                } else {
                    return null;
                }
            }
        }
    }

    public String createAccount() throws SQLException, IOException {
        //User creator text
        StringBuilder createAccountString = clientHandler.writeASC("resources/create_account.txt");
        createAccountString.append("Insert your username (under 50 characters):");
        clientHandler.dataOutputStream.writeUTF(createAccountString.toString());
        String newUsername = clientHandler.dataInputStream.readUTF();
        statement = connection.createStatement();
        String findUserQuery = String.format("SELECT * FROM Users WHERE username == '%s';", newUsername);
        ResultSet userFound = statement.executeQuery(findUserQuery);
        //receive new username/password
        if(userFound.next() == true){
            clientHandler.dataOutputStream.writeUTF("User already exists, please try another username.");
            return createAccount();
        } else {
            clientHandler.dataOutputStream.writeUTF("\nInsert your password (under 50 characters):");
            String newPassword = clientHandler.dataInputStream.readUTF();
            String newUserQuery = String.format("INSERT INTO Users (username,password,wallet) VALUES('%s','%s', 1000)", newUsername, newPassword);
            statement.executeUpdate(newUserQuery);
            statement.close();
            return newUsername;
        }
    }
}
