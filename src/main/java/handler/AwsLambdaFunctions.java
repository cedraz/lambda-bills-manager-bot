package handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import handler.dynamo.DynamoDB;
import handler.expense.ExpenseRepository;
import handler.handlers.expense_handlers.AddExpenseHandler;
import handler.handlers.StartHandler;
import handler.telegram.TelegramBot;
import handler.telegram.Update;
import handler.user.UserRepository;

import java.net.http.HttpClient;
import java.util.Map;

public class AwsLambdaFunctions implements RequestHandler<Map<String, Object>, String> {

    private static final Gson gson = new Gson();
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final TelegramBot telegramBot = new TelegramBot(httpClient);
    private static final DynamoDB dynamoDB = new DynamoDB();
    private static final UserRepository userRepository = new UserRepository(dynamoDB);
    private static final ExpenseRepository expenseRepository = new ExpenseRepository(dynamoDB);

    @Override
    public String handleRequest(Map<String, Object> input, Context context) {
        var logger = context.getLogger();
        logger.log("Evento completo do API Gateway recebido.");

        String requestBody = (String) input.get("body");
        Update update = gson.fromJson(requestBody, Update.class);

        if (update.message == null || update.message.text == null) {
            logger.log("Update ignorado (não é mensagem de texto).");
            return "Ok";
        }

        try {
            String receivedText = update.message.text;

            if (receivedText.startsWith("/start")) {
                StartHandler startHandler = new StartHandler(telegramBot, userRepository);
                startHandler.handle(update, context);
            } else if (receivedText.startsWith("/adicionarDespesa")) {
                AddExpenseHandler addExpenseHandler = new AddExpenseHandler(telegramBot, expenseRepository, userRepository);
                addExpenseHandler.handle(update, context);
            } else {
                long chatId = update.message.chat.id;
                telegramBot.sendMessageToTelegram(chatId, "Comando não reconhecido. Use /start para começar.", context);
            }
        } catch (Exception e) {
            logger.log("ERRO CRÍTICO DURANTE A EXECUÇÃO DO HANDLER: " + e.getMessage());

            long chatId = update.message.chat.id;
            telegramBot.sendMessageToTelegram(chatId, "Desculpe, ocorreu um erro interno. A equipe de suporte já foi notificada.", context);
        }

        return "Processado.";
    }
}