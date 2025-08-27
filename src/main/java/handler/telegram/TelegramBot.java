package handler.telegram;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;
import handler.enums.ParseMode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class TelegramBot {
    private final HttpClient httpClient;
    private static final Gson gson = new Gson();

    public TelegramBot(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void sendMessageToTelegram(long chatId, String text, ParseMode parseMode, Context context) {
        String botToken = System.getenv("TELEGRAM_BOT_TOKEN");
        var logger = context.getLogger();

        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("text", text);
        if (parseMode != ParseMode.None) {
            payload.put("parse_mode", parseMode);
        }

        String jsonPayload = gson.toJson(payload);
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            logger.log("Resposta da API do Telegram: Status " + response.statusCode() + ", Body: " + response.body());

        } catch (Exception e) {
            logger.log("ERRO ao enviar mensagem para o Telegram: " + e.getMessage());
        }
    }

    public void sendMessageToTelegram(long chatId, String text, Context context) {
        sendMessageToTelegram(chatId, text, null, context);
    }
}
