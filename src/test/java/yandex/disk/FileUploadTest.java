package yandex.disk;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DisplayName("Загрузка и публикация файлов")
public class FileUploadTest extends BaseApiTest {

    private String filePath;

    @BeforeEach
    public void preparePaths() {
        filePath = uniqueFile(".txt");
    }

    @AfterEach
    public void cleanup() throws IOException, InterruptedException {
        if (client != null) {
            client.deleteResource(filePath);
        }
    }

    @Test
    @DisplayName("GET /resources/upload — возвращает 200 и ссылку для загрузки")
    public void getUploadLink_returns200AndHref() throws IOException, InterruptedException {
        HttpResponse<String> response = client.getUploadLink(filePath, true);

        assertEquals(200, response.statusCode(), "Получение ссылки должно вернуть 200");

        JsonNode body = MAPPER.readTree(response.body());
        assertTrue(body.has("href"), "Ответ должен содержать href");
        assertNotNull(body.get("href").asText(), "href не должен быть null");
        assertTrue(body.get("href").asText().startsWith("http"), "href должен быть URL");
    }

    @Test
    @DisplayName("Полный цикл загрузки: ссылка → PUT файла → файл существует")
    public void uploadFile_fullCycle() throws IOException, InterruptedException {
        HttpResponse<String> linkResponse = client.getUploadLink(filePath, true);
        assertEquals(200, linkResponse.statusCode());
        String href = MAPPER.readTree(linkResponse.body()).get("href").asText();

        byte[] content = "Hello, Yandex.Disk!".getBytes(StandardCharsets.UTF_8);
        HttpResponse<String> upload = client.uploadToHref(href, content);
        assertTrue(
                upload.statusCode() == 201 || upload.statusCode() == 202,
                "Загрузка должна вернуть 201 или 202, вернула " + upload.statusCode());

        HttpResponse<String> meta = client.getResourceMeta(filePath);
        assertEquals(200, meta.statusCode(), "Загруженный файл должен существовать");
        JsonNode metaBody = MAPPER.readTree(meta.body());
        assertEquals("file", metaBody.get("type").asText(), "Тип ресурса должен быть file");
    }

    @Test
    @DisplayName("PUT /resources/publish — публикует файл, появляется public_url")
    public void publishFile_returnsPublicUrl() throws IOException, InterruptedException {
        String href = MAPPER.readTree(client.getUploadLink(filePath, true).body()).get("href").asText();
        client.uploadToHref(href, "data".getBytes(StandardCharsets.UTF_8));

        HttpResponse<String> publish = client.publishResource(filePath);
        assertTrue(
                publish.statusCode() == 200 || publish.statusCode() == 201,
                "publish должен вернуть 200 или 201, вернул " + publish.statusCode());

        JsonNode meta = MAPPER.readTree(client.getResourceMeta(filePath).body());
        assertTrue(meta.has("public_url"), "У опубликованного файла должен быть public_url");

        HttpResponse<String> unpublish = client.unpublishResource(filePath);
        assertTrue(
                unpublish.statusCode() == 200 || unpublish.statusCode() == 201,
                "unpublish должен вернуть 200 или 201, вернул " + unpublish.statusCode());
    }
}
