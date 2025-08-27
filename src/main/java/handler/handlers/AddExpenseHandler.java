package handler.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import handler.Update;
import handler.expense.Expense;
import handler.expense.ExpenseRepository;
import handler.telegram.TelegramBot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AddExpenseHandler extends BaseHandler {
    private final ExpenseRepository expenseRepository;

    public AddExpenseHandler(TelegramBot telegramBot, ExpenseRepository expenseRepository) {
        super(telegramBot);
        this.expenseRepository = expenseRepository;
    }

    @Override
    public void handle(Update update, Context context) {
        var logger = context.getLogger();
        try {
            Expense expense = validateAndParseExpense(update, context);
            long chatId = update.message.chat.id;
            this.expenseRepository.saveExpense(chatId, expense);
            String response = "Despesa adicionada com sucesso: " + expense.getDescription() + " - R$" + (expense.getAmountAsString()) + " na categoria " + expense.getCategory();
            this.telegramBot.sendMessageToTelegram(chatId, response, context);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return;
            }

            logger.log("ERROR saving expense: " + e.getMessage());
            long chatId = update.message.chat.id;
            String response = "Erro ao adicionar despesa. Verifique o formato e tente novamente.";
            this.telegramBot.sendMessageToTelegram(chatId, response, context);
        }
    }

    private Expense validateAndParseExpense(Update update, Context context) {
        var logger = context.getLogger();
        String message = update.message.text;
        long chatId = update.message.chat.id;
        ArrayList<String> parts = new ArrayList<>(List.of(message.split(" ")));

        if (parts.size() < 3) {
            logger.log("Invalid expense format received.");
            String response = "Formato inválido. Use: /adicionarDespesa <valor>* <descrição>* <categoria> | '*' -> propriedades obrigatórias";
            this.telegramBot.sendMessageToTelegram(chatId, response, context);
            throw new IllegalArgumentException("Invalid expense format");
        }

        String amountPart = parts.get(1);
        String descriptionPart = parts.get(2);
        String categoryPart = parts.size() >= 4 ? parts.get(3) : "Outros";
        int amount = Integer.parseInt(amountPart); // Convert to cents
        LocalDate date = LocalDate.now();

        return new Expense(amount, descriptionPart, date, categoryPart);
    }
}
