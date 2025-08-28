package handler.handlers.expense_handlers;

import com.amazonaws.services.lambda.runtime.Context;
import handler.enums.ConversationState;
import handler.handlers.BaseHandler;
import handler.telegram.Update;
import handler.expense.Expense;
import handler.expense.ExpenseRepository;
import handler.telegram.TelegramBot;
import handler.user.User;
import handler.user.UserRepository;

import java.time.LocalDate;

public class AddExpenseHandler extends BaseHandler {
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private User currentUser;

    public AddExpenseHandler(TelegramBot telegramBot, ExpenseRepository expenseRepository, UserRepository userRepository) {
        super(telegramBot);
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    public AddExpenseHandler(TelegramBot telegramBot, ExpenseRepository expenseRepository, UserRepository userRepository, User user) {
        super(telegramBot);
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.currentUser = user;
    }

    @Override
    public void handle(Update update, Context context) {
        long chatId = update.message.chat.id;

        if (this.currentUser == null) {
            this.currentUser = this.userRepository.findByChatId(chatId);
            if (this.currentUser == null) {
                this.telegramBot.sendMessageToTelegram(chatId, "Por favor, inicie uma conversa comigo usando o comando /start.", context);
                return;
            }
        }

        this.currentUser.setConversationState(ConversationState.AWAITING_AMOUNT);
        this.userRepository.updateUser(this.currentUser);

        String response = "Iniciando o processo de adição de despesa.\n" +
                "Envie o valor da despesa (ex: 12,34 ou 12.34).";
        this.telegramBot.sendMessageToTelegram(chatId, response, context);
    }

    public void addAmount(Update update, Context context) {
        long chatId = update.message.chat.id;
        try {
            String message = update.message.text;
            double amountValue = Double.parseDouble(message.trim().replace(",", "."));
            int amountInCents = (int) Math.round(amountValue * 100);

            this.currentUser.setInProgressExpense(new Expense(amountInCents, "", LocalDate.now()));
            this.currentUser.setConversationState(ConversationState.AWAITING_DESCRIPTION);
            this.userRepository.updateUser(this.currentUser);

            String response = "Valor definido como R$ " + String.format("%.2f", amountValue) + ". Agora, envie a descrição da despesa.";
            this.telegramBot.sendMessageToTelegram(chatId, response, context);
        } catch (NumberFormatException e) {
            this.telegramBot.sendMessageToTelegram(chatId, "Valor inválido. Por favor, envie apenas o número (ex: 35.50).", context);
        }
    }

    public void addDescription(Update update, Context context) {
        long chatId = update.message.chat.id;
        String description = update.message.text.trim();

        if (description.isEmpty()) {
            this.telegramBot.sendMessageToTelegram(chatId, "A descrição não pode estar vazia. Por favor, tente novamente.", context);
            return;
        }

        this.currentUser.getInProgressExpense().setDescription(description);
        this.currentUser.setConversationState(ConversationState.AWAITING_CATEGORY);
        this.userRepository.updateUser(this.currentUser);

        String response = "Descrição adicionada. Agora, envie a categoria. (ou digite /pular)";
        this.telegramBot.sendMessageToTelegram(chatId, response, context);
    }

    public void addCategory(Update update, Context context) {
        long chatId = update.message.chat.id;
        String message = update.message.text.trim();

        String category = "Outros";
        if (!message.equalsIgnoreCase("/pular")) {
            category = message;
        }

        Expense finalExpense = this.currentUser.getInProgressExpense();
        finalExpense.setCategory(category);

        this.expenseRepository.saveExpense(chatId, finalExpense);
        this.currentUser.setInProgressExpense(null);
        this.currentUser.setConversationState(ConversationState.NONE);
        this.userRepository.updateUser(this.currentUser);

        String response = "✅ Despesa adicionada com sucesso na categoria: " + category + ".";
        this.telegramBot.sendMessageToTelegram(chatId, response, context);
    }
}
