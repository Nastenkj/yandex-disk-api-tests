package yandex.disk;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;

import java.util.UUID;


public abstract class BaseApiTest {

    protected static final ObjectMapper MAPPER = new ObjectMapper();

    protected String token;
    protected YandexDiskClient client;

    @BeforeEach
    public void initClient() {
        token = resolveToken();
        Assumptions.assumeTrue(
                token != null && !token.isBlank(),
                "Токен не задан: установите переменную окружения YANDEX_DISK_TOKEN " +
                        "или запустите с -Dyandex.token=...");
        client = new YandexDiskClient(token);
    }

    protected String uniqueFolder() {
        return "/yatest-folder-" + UUID.randomUUID();
    }

    protected String uniqueFile(String extension) {
        return "/yatest-file-" + UUID.randomUUID() + extension;
    }

    private static String resolveToken() {
        String value = System.getenv("YANDEX_DISK_TOKEN");
        if (value == null || value.isBlank()) {
            value = System.getProperty("yandex.token");
        }
        return value;
    }
}
