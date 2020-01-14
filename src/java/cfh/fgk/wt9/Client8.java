package cfh.fgk.wt9;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class Client8 implements Client {

    private final Settings settings = Settings.instance;

    @Override
    public String get(String addr) throws IOException, InterruptedException {
        URL url = new URL(settings.url() + addr);
        URLConnection connection = url.openConnection();
        connection.connect();
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
        }
        if (connection instanceof HttpURLConnection) {
            HttpURLConnection c = (HttpURLConnection) connection;
            if (c.getResponseCode() != 200)
                throw new IOException("Error " + c.getResponseCode());
        }
        return builder.toString();
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client8();
        System.out.println(
        client.get("")
        );
    }
}
