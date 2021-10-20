import static org.junit.Assert.fail;

import org.junit.Test;

public class Test{
    @Test
    public void givenGreetingClient_whenServerRespondsWhenStarted_thenCorrect() {
        Client client = new Client();
        client.startConnection("127.0.0.1", 6666);
        String response = client.sendMessage("hello server");
        assertEquals("hello client", response);
    }
}