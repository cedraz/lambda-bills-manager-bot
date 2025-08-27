package handler.telegram;

import com.amazonaws.services.lambda.runtime.Context;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TelegramBot {
    private final HttpClient httpClient;

    public TelegramBot(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void sendMessageToTelegram(long chatId, String text, Context context) {
        String botToken = System.getenv("TELEGRAM_BOT_TOKEN");
        var logger = context.getLogger();

        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        String jsonPayload = String.format("{\"chat_id\": %d, \"text\": \"%s\"}", chatId, text);

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
}
