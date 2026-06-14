package yandex.disk;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Операции с папками")
public class FolderCrudTest extends BaseApiTest {

    private String folderPath;
    private String secondPath;

    @BeforeEach
    public void preparePaths() {
        folderPath = uniqueFolder();
        secondPath = folderPath + "-2";
    }

    @AfterEach
    public void cleanup() throws IOException, InterruptedException {
        if (client != null) {
            client.deleteResource(folderPath);
            client.deleteResource(secondPath);
        }
    }

    @Test
    @DisplayName("PUT /resources — создаёт папку (201), повтор — 409")
    public void createFolder_returns201ThenConflict409() throws IOException, InterruptedException {
        HttpResponse<String> created = client.createFolder(folderPath);
        assertEquals(201, created.statusCode(), "Первое создание должно вернуть 201");

        HttpResponse<String> duplicate = client.createFolder(folderPath);
        assertEquals(409, duplicate.statusCode(), "Повторное создание должно вернуть 409");
    }

    @Test
    @DisplayName("POST /resources/move — перемещает папку (201/202)")
    public void moveFolder_returns201Or202() throws IOException, InterruptedException {
        assertEquals(201, client.createFolder(folderPath).statusCode());

        HttpResponse<String> moved = client.moveResource(folderPath, secondPath);
        assertTrue(
                moved.statusCode() == 201 || moved.statusCode() == 202,
                "move должен вернуть 201 или 202, вернул " + moved.statusCode());

        assertEquals(200, client.getResourceMeta(secondPath).statusCode(),
                "Перемещённая папка должна существовать");
    }

    @Test
    @DisplayName("POST /resources/copy — копирует папку (201/202), оригинал остаётся")
    public void copyFolder_returns201Or202AndKeepsOriginal() throws IOException, InterruptedException {
        assertEquals(201, client.createFolder(folderPath).statusCode());

        HttpResponse<String> copied = client.copyResource(folderPath, secondPath);
        assertTrue(
                copied.statusCode() == 201 || copied.statusCode() == 202,
                "copy должен вернуть 201 или 202, вернул " + copied.statusCode());

        assertEquals(200, client.getResourceMeta(folderPath).statusCode(), "Оригинал должен остаться");
        assertEquals(200, client.getResourceMeta(secondPath).statusCode(), "Копия должна появиться");
    }

    @Test
    @DisplayName("DELETE /resources — удаляет папку (204/202), после — 404")
    public void deleteFolder_returns204Or202() throws IOException, InterruptedException {
        assertEquals(201, client.createFolder(folderPath).statusCode());

        HttpResponse<String> deleted = client.deleteResource(folderPath);
        assertTrue(
                deleted.statusCode() == 204 || deleted.statusCode() == 202,
                "delete должен вернуть 204 или 202, вернул " + deleted.statusCode());

        if (deleted.statusCode() == 204) {
            assertEquals(404, client.getResourceMeta(folderPath).statusCode(),
                    "После удаления папки быть не должно");
        }
    }
}
