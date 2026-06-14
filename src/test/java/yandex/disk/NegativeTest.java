package yandex.disk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DisplayName("Негативные сценарии")
public class NegativeTest extends BaseApiTest {

    @Test
    @DisplayName("Невалидный токен — 401 Unauthorized")
    public void invalidToken_returns401() throws IOException, InterruptedException {
        YandexDiskClient badClient = new YandexDiskClient("invalid-token-" + UUID.randomUUID());
        HttpResponse<String> response = badClient.getDiskInfo();
        assertEquals(401, response.statusCode(), "С невалидным токеном должно быть 401");
    }

    @Test
    @DisplayName("GET несуществующего ресурса — 404")
    public void getMissingResource_returns404() throws IOException, InterruptedException {
        HttpResponse<String> response = client.getResourceMeta("/no-such-" + UUID.randomUUID());
        assertEquals(404, response.statusCode(), "Несуществующий ресурс должен дать 404");
    }

    @Test
    @DisplayName("DELETE несуществующего ресурса — 404")
    public void deleteMissingResource_returns404() throws IOException, InterruptedException {
        HttpResponse<String> response = client.deleteResource("/no-such-" + UUID.randomUUID());
        assertEquals(404, response.statusCode(), "Удаление несуществующего должно дать 404");
    }

    @Test
    @DisplayName("Создание папки в несуществующем родителе — 409")
    public void createInMissingParent_returns409() throws IOException, InterruptedException {
        String path = "/no-such-parent-" + UUID.randomUUID() + "/child";
        HttpResponse<String> response = client.createFolder(path);
        assertEquals(409, response.statusCode(),
                "Создание во вложенной несуществующей папке должно дать 409");
    }

    @Test
    @DisplayName("Копирование несуществующего источника — 404")
    public void copyMissingSource_returns404() throws IOException, InterruptedException {
        String from = "/no-such-source-" + UUID.randomUUID();
        String to = "/dest-" + UUID.randomUUID();
        HttpResponse<String> response = client.copyResource(from, to);
        assertEquals(404, response.statusCode(),
                "Копирование несуществующего источника должно дать 404");
    }
}
