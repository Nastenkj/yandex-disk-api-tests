package yandex.disk;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Информация о диске")
public class DiskInfoTest extends BaseApiTest {

    @Test
    @DisplayName("GET /v1/disk — возвращает 200 и квоту")
    public void getDiskInfo_returns200AndQuota() throws IOException, InterruptedException {
        HttpResponse<String> response = client.getDiskInfo();

        assertEquals(200, response.statusCode(), "GET /v1/disk должен вернуть 200");

        JsonNode body = MAPPER.readTree(response.body());
        assertTrue(body.has("total_space"), "Тело ответа должно содержать total_space");
        assertTrue(body.has("used_space"), "Тело ответа должно содержать used_space");
        assertTrue(body.get("total_space").asLong() > 0, "total_space должно быть положительным");
        assertTrue(
                body.get("used_space").asLong() <= body.get("total_space").asLong(),
                "used_space не может превышать total_space");
    }

    @Test
    @DisplayName("GET /resources/files — возвращает 200 и список файлов с учётом limit")
    public void getFilesList_returns200AndRespectsLimit() throws IOException, InterruptedException {
        int limit = 5;
        HttpResponse<String> response = client.getFilesList(limit);

        assertEquals(200, response.statusCode(), "GET /resources/files должен вернуть 200");

        JsonNode body = MAPPER.readTree(response.body());
        assertTrue(body.has("items"), "Ответ должен содержать массив items");
        assertTrue(body.get("items").isArray(), "items должен быть массивом");
        assertTrue(
                body.get("items").size() <= limit,
                "Количество элементов не должно превышать limit");
    }
}
