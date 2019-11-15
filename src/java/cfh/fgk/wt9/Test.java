package cfh.fgk.wt9;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

public class Test {

    private static final String HOME_URL = "http://192.168.4.1/";
    private static final String DOWNLOAD_URL = HOME_URL + "download.html";
    private static final String DOWNLOAD_FORMAT = HOME_URL + "download.html?spage=%d&npage=%d&action=download";
    
    public static void main(String[] args) {
//        get(HOME_URL);
//        System.out.println();
//        get(DOWNLOAD_URL);
//        System.out.println();
//        get(String.format(DOWNLOAD_FORMAT, 1, 5));
        download(DOWNLOAD_FORMAT, 1, 6);
}
    
    private static void get(String url) {
        HttpClient client = HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .build();
        try {
            var response = client.send(request, BodyHandlers.ofLines());
            System.out.println(response.statusCode());
            response.body().forEach(System.out::println);
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
            return;
        }
    }
    
    private static void download(String format, int start, int count) {
        HttpClient client = HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .build();
        StringBuilder data = new StringBuilder();
        for (var i = 0; i < count; i++) {
            String url = String.format(format, start+i, 1);
            var request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .build();
            try {
                var response = client.send(request, BodyHandlers.ofString());
                System.out.printf("%5d: %4s - %s%n", start+i, response.statusCode(), url);
                if (response.statusCode() != 200)
                    break;
                data.append(response.body());
            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();
                return;
            }
        }
        System.out.println();
        System.out.println(data);
        System.out.println();
    }
}
