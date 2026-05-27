package me.zyouime.nvpmod.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.zyouime.nvpmod.Main;
import me.zyouime.nvpmod.config.ModConfig;
import me.zyouime.nvpmod.enums.Action;
import me.zyouime.nvpmod.enums.ResponseStatus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

public class HttpUtil {

    private final ModConfig config;
    private final HttpClient client = HttpClient.newHttpClient();
    private final String link = "https://zyouime.top/";

    public HttpUtil(ModConfig config) {
        this.config = config;
    }

    public void getProfile() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(link + "profile"))
                    .header("Authorization", "Basic " + encodedAuth(config.username, config.password))
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> handleResponse(response, () -> {
                        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                        JsonObject data = json.get("data").getAsJsonObject();
                        StringBuilder builder = new StringBuilder();
                        String enumPrefix = "* ";
                        builder.append(Main.PREFIX).append("Статистика ").append(config.username);
                        for (Action action : Action.values()) {
                            int count = data.has(action.name()) ? data.get(action.name()).getAsInt() : 0;
                            builder.append("\n").append(enumPrefix).append(action.desc).append(": ").append(count);
                        }
                        MinecraftClient.getInstance().player.sendMessage(Text.literal(builder.toString()));
                    }))
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendAction(Action action, String triggerPlayer) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(link + "action"))
                    .header("Authorization", "Basic " + encodedAuth(config.username, config.password))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(buildJson(action, triggerPlayer)))
                    .build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> handleResponse(response,() -> {
                        JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
                        ResponseStatus status = EnumUtil.fromString(ResponseStatus.class, responseJson.get("status").getAsString());
                        if (status == ResponseStatus.FAILURE) {
                            Main.errorMsg(responseJson.get("reason").getAsString());
                        }
                    }))
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String encodedAuth(String username, String password) {
        return  Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

    private void handleResponse(HttpResponse<String> response, Runnable successRunnable) {
        int statusCode = response.statusCode();
        switch (statusCode) {
            case 200 -> successRunnable.run();
            case 401 -> Main.errorMsg("Ошибка авторизации. Введи корректные данные. §f(/nvp auth <login:pass>)");
            default -> Main.errorMsg("Неизвестная ошибка. Код " + statusCode);
        }
    }

    private String buildJson(Action action, String triggerPlayer) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", action.name());
        jsonObject.addProperty("triggerPlayer", triggerPlayer);
        return jsonObject.toString();
    }
}
