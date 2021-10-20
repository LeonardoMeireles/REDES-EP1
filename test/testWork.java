import org.junit.Assert;
import org.junit.Test;


import java.io.IOException;

public class testWork{
    @Test
    public void givenGreetingClient_whenServerRespondsWhenStarted_thenCorrect() throws IOException {
        Client client = new Client();
        client.startConnection("192.168.0.119", 6666);
        String response = client.sendMessage("hello server");
        Assert.assertEquals("hello client", response);
    }
}
