package vp.shorturl.app;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CliIntegrationTest {

    @Test
    public void testCliScenario() throws Exception {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        PipedOutputStream pipedOut = new PipedOutputStream();
        PipedInputStream pipedIn = new PipedInputStream(pipedOut);

        System.setIn(pipedIn);

        Thread mainThread = new Thread(() -> {
            try {
                Main.main(new String[]{});
            } catch (Exception ignored) { }
        });

        mainThread.start();

        send(pipedOut, "1\n");
        Thread.sleep(200);

        String console1 = output.toString(StandardCharsets.UTF_8);
        int idx = console1.indexOf("Your UUID is: ");
        assertTrue(idx > 0);

        String uuidStr = console1.substring(idx + 14, idx + 14 + 36);
        UUID.fromString(uuidStr);

        send(pipedOut, "2\n");
        send(pipedOut, uuidStr + "\n");
        Thread.sleep(200);

        send(pipedOut, "3\n");
        send(pipedOut, "https://example.com\n");
        Thread.sleep(200);

        send(pipedOut, "5\n");
        Thread.sleep(200);

        send(pipedOut, "0\n");
        Thread.sleep(200);

        mainThread.interrupt();
        System.setOut(originalOut);

        String full = output.toString(StandardCharsets.UTF_8);

        assertTrue(full.contains("New user created"));
        assertTrue(full.contains("Logged in as user"));
        assertTrue(full.contains("Short link created"));
        assertTrue(full.contains("Your links"));
    }

    private void send(PipedOutputStream out, String s) throws IOException {
        out.write(s.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }
}
