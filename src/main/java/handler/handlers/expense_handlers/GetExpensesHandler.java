package handler.handlers.expense_handlers;

import com.amazonaws.services.lambda.runtime.Context;
import handler.enums.ParseMode;
import handler.expense.Expense;
import handler.expense.ExpenseRepository;
import handler.handlers.BaseHandler;
import handler.telegram.TelegramBot;
import handler.telegram.Update;

import java.util.List;
import java.util.stream.Collectors;

public class GetExpensesHandler extends BaseHandler {
    private final ExpenseRepository expenseRepository;

    public GetExpensesHandler(TelegramBot telegramBot, ExpenseRepository expenseRepository) {
        super(telegramBot);
        this.expenseRepository = expenseRepository;
    }

    @Override
    public void handle(Update update, Context context) {
        var logger = context.getLogger();
        try {
            long chatId = update.message.chat.id;
            List<Expense> expenses = this.expenseRepository.getExpenses(chatId);

            if (expenses.isEmpty()) {
                String response = "Você não tem despesas registradas.";
                this.telegramBot.sendMessageToTelegram(chatId, response, context);
                return;
            }

            // O método .map(Expense::toString) já retorna a string formatada
            String expenseList = expenses.stream()
                    .map(Expense::toString)
                    .collect(Collectors.joining("\n\n--------------------\n\n"));

            String response = "🧾 *Suas despesas recentes:*\n\n" + expenseList;

            this.telegramBot.sendMessageToTelegram(chatId, response, ParseMode.HTML, context);
        } catch (Exception e) {
            logger.log("ERROR retrieving expenses: " + e.getMessage());
            long chatId = update.message.chat.id;
            String response = "Erro ao listar despesas. Tente novamente mais tarde.";
            this.telegramBot.sendMessageToTelegram(chatId, response, context);
        }
    }
}
