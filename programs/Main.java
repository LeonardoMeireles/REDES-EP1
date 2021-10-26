package programs;

import java.io.IOException;

class Main{
    public static void main(String[] args) throws IOException{
        Server server = new Server(5056);
        server.run();
    }

}