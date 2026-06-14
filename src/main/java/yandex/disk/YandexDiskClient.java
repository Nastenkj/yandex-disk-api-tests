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
        return send(baseRequest(URI.create(BASE_URL)).GET().build());
    }

    public HttpResponse<String> createFolder(String path) throws IOException, InterruptedException {
        return send(baseRequest(resourceUri(path)).PUT(HttpRequest.BodyPublishers.noBody()).build());
    }

    public HttpResponse<String> getResourceMeta(String path) throws IOException, InterruptedException {
        return send(baseRequest(resourceUri(path)).GET().build());
    }

    public HttpResponse<String> moveResource(String from, String to) throws IOException, InterruptedException {
        URI uri = URI.create(BASE_URL + "/resources/move?from=" + encode(from) + "&path=" + encode(to));
        return send(baseRequest(uri).POST(HttpRequest.BodyPublishers.noBody()).build());
    }

    public HttpResponse<String> copyResource(String from, String to) throws IOException, InterruptedException {
        URI uri = URI.create(BASE_URL + "/resources/copy?from=" + encode(from) + "&path=" + encode(to));
        return send(baseRequest(uri).POST(HttpRequest.BodyPublishers.noBody()).build());
    }

    public HttpResponse<String> deleteResource(String path) throws IOException, InterruptedException {
        return send(baseRequest(resourceUri(path)).DELETE().build());
    }

    public HttpResponse<String> getFilesList(int limit) throws IOException, InterruptedException {
        URI uri = URI.create(BASE_URL + "/resources/files?limit=" + limit);
        return send(baseRequest(uri).GET().build());
    }

    public HttpResponse<String> getUploadLink(String path, boolean overwrite)
            throws IOException, InterruptedException {
        URI uri = URI.create(BASE_URL + "/resources/upload?path=" + encode(path) + "&overwrite=" + overwrite);
        return send(baseRequest(uri).GET().build());
    }

    public HttpResponse<String> uploadToHref(String href, byte[] content)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(href))
                .timeout(Duration.ofSeconds(20))
                .PUT(HttpRequest.BodyPublishers.ofByteArray(content))
                .build();
        return send(request);
    }

    public HttpResponse<String> publishResource(String path) throws IOException, InterruptedException {
        URI uri = URI.create(BASE_URL + "/resources/publish?path=" + encode(path));
        return send(baseRequest(uri).PUT(HttpRequest.BodyPublishers.noBody()).build());
    }

    public HttpResponse<String> unpublishResource(String path) throws IOException, InterruptedException {
        URI uri = URI.create(BASE_URL + "/resources/unpublish?path=" + encode(path));
        return send(baseRequest(uri).PUT(HttpRequest.BodyPublishers.noBody()).build());
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
