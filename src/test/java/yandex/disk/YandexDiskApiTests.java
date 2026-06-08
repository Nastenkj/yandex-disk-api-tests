package yandex.disk;
 
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
 
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.UUID;
 
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
 
public class YandexDiskApiTests {
 
    private static final ObjectMapper MAPPER = new ObjectMapper();
 
    private String token;
    private YandexDiskClient client;
    private String folderPath;
    private String movedFolderPath;
 
    @BeforeEach
    public void setUp() {
        token = resolveToken();
        Assumptions.assumeTrue(
                token != null && !token.isBlank(),
                "Токен не задан: установите переменную окружения YANDEX_DISK_TOKEN " +
                        "или запустите с -Dyandex.token=...");
        client = new YandexDiskClient(token);
        folderPath = "/yatest-folder-" + UUID.randomUUID();
        movedFolderPath = folderPath + "-moved";
    }
 
    @AfterEach
    public void cleanup() throws IOException, InterruptedException {
        if (client != null) {
            client.deleteResource(folderPath);
            client.deleteResource(movedFolderPath);
        }
    }
 
    @Test
    @DisplayName("GET /v1/disk — возвращает 200 и квоту диска")
    public void getDiskInfo_returns200AndQuota() throws IOException, InterruptedException {
        HttpResponse<String> response = client.getDiskInfo();
 
        assertEquals(200, response.statusCode(), "GET /v1/disk должен вернуть 200");
 
        JsonNode body = MAPPER.readTree(response.body());
        assertTrue(body.has("total_space"), "Тело ответа должно содержать total_space");
        assertTrue(body.has("used_space"), "Тело ответа должно содержать used_space");
        assertTrue(body.get("total_space").asLong() > 0, "total_space должно быть положительным");
    }
 
    @Test
    @DisplayName("PUT /resources — создаёт папку (201), повторно — конфликт (409)")
    public void createFolder_returns201ThenConflict409() throws IOException, InterruptedException {
        HttpResponse<String> created = client.createFolder(folderPath);
        assertEquals(201, created.statusCode(), "Первое создание папки должно вернуть 201");
 
        HttpResponse<String> duplicate = client.createFolder(folderPath);
        assertEquals(409, duplicate.statusCode(), "Повторное создание той же папки должно вернуть 409");
    }
 
    @Test
    @DisplayName("GET /resources — несуществующий ресурс возвращает 404")
    public void getResourceMeta_notFoundReturns404() throws IOException, InterruptedException {
        HttpResponse<String> response = client.getResourceMeta("/no-such-folder-" + UUID.randomUUID());
        assertEquals(404, response.statusCode(), "Запрос несуществующего ресурса должен вернуть 404");
    }
 
    @Test
    @DisplayName("POST /resources/move — перемещает папку (201/202)")
    public void moveFolder_returns201Or202() throws IOException, InterruptedException {
        HttpResponse<String> created = client.createFolder(folderPath);
        assertEquals(201, created.statusCode(), "Папка должна быть создана перед перемещением");
 
        HttpResponse<String> moved = client.moveResource(folderPath, movedFolderPath);
        assertTrue(
                moved.statusCode() == 201 || moved.statusCode() == 202,
                "POST /resources/move должен вернуть 201 или 202, а вернул " + moved.statusCode());
 
        HttpResponse<String> meta = client.getResourceMeta(movedFolderPath);
        assertEquals(200, meta.statusCode(), "Перемещённая папка должна существовать (200)");
    }
 
    @Test
    @DisplayName("DELETE /resources — удаляет папку (204/202), после удаления 404")
    public void deleteFolder_returns204Or202() throws IOException, InterruptedException {
        HttpResponse<String> created = client.createFolder(folderPath);
        assertEquals(201, created.statusCode(), "Папка должна быть создана перед удалением");
 
        HttpResponse<String> deleted = client.deleteResource(folderPath);
        assertTrue(
                deleted.statusCode() == 204 || deleted.statusCode() == 202,
                "DELETE /resources должен вернуть 204 или 202, а вернул " + deleted.statusCode());
 
        if (deleted.statusCode() == 204) {
            HttpResponse<String> meta = client.getResourceMeta(folderPath);
            assertEquals(404, meta.statusCode(), "После удаления папка не должна существовать (404)");
        }
    }
 
    private static String resolveToken() {
        String value = System.getenv("YANDEX_DISK_TOKEN");
        if (value == null || value.isBlank()) {
            value = System.getProperty("yandex.token");
        }
        return value;
    }
}