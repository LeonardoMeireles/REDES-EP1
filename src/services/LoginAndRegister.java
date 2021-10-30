package programs.services;

import ClientHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class LoginAndRegister{

    ClientHandler clientHandler;

    public LoginAndRegister(ClientHandler clientHandler){
        this.clientHandler = clientHandler;
    }


    public void login(String username) throws FileNotFoundException, IOException{
        File dataBase = new File("database");
        File [] list = dataBase.listFiles();

        //search for username && create new account if username is not found
        boolean userFound = false;
        for(File dir : list){
            String usernameFile = dir.getName();
            if( (username+".txt").equals(usernameFile)){
                userFound = true;
                break;
            }
        }
        // Logging in user
        if(userFound){
            clientHandler.dataOutputStream.writeUTF("Insert your password:");
            clientHandler.dataOutputStream.flush();
            String password = clientHandler.dataInputStream.readUTF();
        } else { // No user found
            clientHandler.dataOutputStream.writeUTF("\nHuum, i don't know any adventurer with that name, would you like to sign up for my services?\nY: yes\tN: no\n");
            clientHandler.dataOutputStream.flush();
            while(true){
                String answer = clientHandler.dataInputStream.readUTF();
                if(answer.toLowerCase().equals("y")){
                    createAccount();
                } else if(answer.toLowerCase().equals("n")){
                    clientHandler.dataOutputStream.writeUTF("Insert your username:\n");
                    clientHandler.dataOutputStream.flush();
                    String newUsername = clientHandler.dataInputStream.readUTF();
                    login(newUsername);
                } else{
                    clientHandler.dataOutputStream.writeUTF("\nDidn't quite get that traveler, do you want to use my service?\nY: yes\tN: no\n");
                    clientHandler.dataOutputStream.flush();
                    continue;
                }
            }
        }
    }

    public void createAccount() throws FileNotFoundException, IOException {
        StringBuilder createAccountString = clientHandler.escreveASC("resources/create_account.txt");
        createAccountString.append("Insert your username:\n");
        String newUsername = clientHandler.dataInputStream.readUTF();
        createAccountString.append("Insert your password:\n");
        new File("resourcces/database"+newUsername).mkdirs();
    }

}
