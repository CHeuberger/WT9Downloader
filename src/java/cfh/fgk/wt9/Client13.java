package cfh.fgk.wt9;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;


public class Client13 implements Client {
    
    private final Settings settings = Settings.instance;
    private final HttpClient client;
    
    public Client13() {
        client = HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(settings.timeout()))
                .build();
    }
    
    @Override
    public String get(String addr) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(settings.url() + addr))
                .timeout(Duration.ofSeconds(settings.timeout()))
                .build();
        System.out.println(request);
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        System.out.println(response);
        int status = response.statusCode();
        if (status == 200) {
            return response.body();
        } else {
            throw new IOException("Error " + status);
        }
    }
}
