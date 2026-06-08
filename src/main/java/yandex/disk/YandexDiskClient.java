package yandex.disk;
 
import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
 
public class YandexDiskClient {
 
    private static final String BASE_URL = "https://cloud-api.yandex.net/v1/disk";
 
    private final String token;
    private final HttpClient client;
 
    public YandexDiskClient(String token) {
        this.token = token;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .proxy(ProxySelector.of(null))
                .build();
    }
 
    public HttpResponse<String> getDiskInfo() throws IOException, InterruptedException {
        HttpRequest request = baseRequest(URI.create(BASE_URL))
                .GET()
                .build();
        return send(request);
    }
 
    public HttpResponse<String> createFolder(String path) throws IOException, InterruptedException {
        HttpRequest request = baseRequest(resourceUri(path))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        return send(request);
    }
 
    public HttpResponse<String> getResourceMeta(String path) throws IOException, InterruptedException {
        HttpRequest request = baseRequest(resourceUri(path))
                .GET()
                .build();
        return send(request);
    }
 
    public HttpResponse<String> moveResource(String from, String to) throws IOException, InterruptedException {
        URI uri = URI.create(BASE_URL + "/resources/move?from=" + encode(from) + "&path=" + encode(to));
        HttpRequest request = baseRequest(uri)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return send(request);
    }
 
    public HttpResponse<String> deleteResource(String path) throws IOException, InterruptedException {
        HttpRequest request = baseRequest(resourceUri(path))
                .DELETE()
                .build();
        return send(request);
    }
 
    private HttpRequest.Builder baseRequest(URI uri) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "OAuth " + token)
                .header("Accept", "application/json");
    }
 
    private HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
 
    private static URI resourceUri(String path) {
        return URI.create(BASE_URL + "/resources?path=" + encode(path));
    }
 
    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}