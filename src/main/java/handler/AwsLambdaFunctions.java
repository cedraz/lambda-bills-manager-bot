package handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import handler.dynamo.DynamoDB;
import handler.enums.ConversationState;
import handler.expense.ExpenseRepository;
import handler.handlers.HelpHandler;
import handler.handlers.expense_handlers.AddExpenseHandler;
import handler.handlers.StartHandler;
import handler.handlers.expense_handlers.GetExpensesHandler;
import handler.telegram.TelegramBot;
import handler.telegram.Update;
import handler.user.User;
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

        logger.log("Request body: " + requestBody);

        long chatId = update.message.chat.id;

        try {
            Boolean answered = this.handleCommand(update, context);

            if (answered) {
                return "Comando processado.";
            }

            User user = userRepository.findByChatId(chatId);

            if (user == null) {
                telegramBot.sendMessageToTelegram(chatId, "Por favor, inicie uma conversa comigo usando o comando /start.", context);
                return "Usuário não registrado.";
            }

            if (user.getConversationState() == ConversationState.NONE) {
                telegramBot.sendMessageToTelegram(chatId, "Por favor, use um dos comandos disponíveis. Digite /ajuda para ver os comandos.", context);
                return "Usuário em estado NONE.";
            }

            AddExpenseHandler addExpenseHandler = new AddExpenseHandler(telegramBot, expenseRepository, userRepository, user);

            switch (user.getConversationState()) {
                case ConversationState.AWAITING_AMOUNT:
                    addExpenseHandler.addAmount(update, context);
                    break;
                case ConversationState.AWAITING_DESCRIPTION:
                    addExpenseHandler.addDescription(update, context);
                    break;
                case ConversationState.AWAITING_CATEGORY:
                    addExpenseHandler.addCategory(update, context);
                    break;
                default:
                    telegramBot.sendMessageToTelegram(chatId, "Estado de conversa desconhecido. Por favor, use /ajuda para ver os comandos.", context);
                    user.setConversationState(ConversationState.NONE);
                    userRepository.saveUser(user);
                    break;
            }

        } catch (Exception e) {
            logger.log("ERRO CRÍTICO DURANTE A EXECUÇÃO DO HANDLER: " + e.getMessage());

            telegramBot.sendMessageToTelegram(chatId, "Desculpe, ocorreu um erro interno. A equipe de suporte já foi notificada.", context);
        }

        return "Processado.";
    }

    private Boolean handleCommand(Update update, Context context) {
        String messageText = update.message.text;

        switch (messageText) {
            case "/start":
                StartHandler startHandler = new StartHandler(telegramBot, userRepository);
                startHandler.handle(update, context);
                return true;
            case "/adicionarDespesa":
                AddExpenseHandler addExpenseHandler = new AddExpenseHandler(telegramBot, expenseRepository, userRepository);
                addExpenseHandler.handle(update, context);
                return true;
            case "/ajuda":
                HelpHandler helpHandler = new HelpHandler(telegramBot);
                helpHandler.handle(update, context);
                return true;
            case "/listarDespesas":
                GetExpensesHandler getExpensesHandler = new GetExpensesHandler(telegramBot, expenseRepository);
                getExpensesHandler.handle(update, context);
                return true;
            default:
                return false;
        }
    }
}