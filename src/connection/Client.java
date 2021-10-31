package connection;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    final static int ServerPort = 1234;
    static Socket socket;

    public static void connect() throws IOException{
        InetAddress ip = InetAddress.getByName("localhost");// get localhost ip
        socket = new Socket(ip, ServerPort);
    }

    public static void createSendMessage(Scanner scan, DataOutputStream dataOutputStream){
        // sendMessage thread
        Thread sendMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String input = scan.nextLine();
                    try {
                        dataOutputStream.writeUTF(input);
                    } catch (IOException error) {
                        error.printStackTrace();
                        System.exit(500);
                        break;
                    }
                }
                try {
                    dataOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        sendMessage.start();
    }
    public static void createReadMessage(DataInputStream dataInputStream){
        // readMessage thread
        Thread readResponse = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        // read the message sent to this client
                        String input = dataInputStream.readUTF();
                        System.out.println(input);
                    } catch (IOException error) {
                        error.printStackTrace();
                        System.exit(500);
                        break;
                    }
                }
                try {
                    dataInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        readResponse.start();
    }

    public static void main(String args[]) throws UnknownHostException, IOException {
        Scanner scan = new Scanner(System.in);

        connect(); // realize the connection

        // obtaining input and out streams
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

        //create threads
        createSendMessage(scan, dataOutputStream);
        createReadMessage(dataInputStream);

    }
}