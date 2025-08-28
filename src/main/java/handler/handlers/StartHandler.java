package handler.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import handler.enums.ParseMode;
import handler.telegram.Update;
import handler.telegram.TelegramBot;
import handler.user.User;
import handler.user.UserRepository;

public class StartHandler extends BaseHandler{
    private final UserRepository userRepository;

    public StartHandler(TelegramBot telegramBot,  UserRepository userRepository) {
        super(telegramBot);
        this.userRepository = userRepository;
    }

    @Override
    public void handle(Update update, Context context) {
        User user = new User(update.message.chat.id, update.message.chat.first_name, update.message.chat.username);
        this.userRepository.saveUser(user);

        String response =
                "Olá, <b>" + user.getFirst_name() + "</b>! Seja bem-vindo(a) ao bot. Você foi registrado com sucesso.\n\n" +
                        "<b>Comandos disponíveis:</b>\n" +
                        "<code>/adicionarDespesa</code> - Inicia o processo para adicionar uma despesa.\n" +
                        "<code>/listarDespesas</code> - Lista suas despesas recentes.\n" +
                        "<code>/removerDespesa</code> - Remove uma despesa pelo ID.\n" +
                        "<code>/ajuda</code> - Mostra esta mensagem de ajuda.";
        this.telegramBot.sendMessageToTelegram(update.message.chat.id, response, ParseMode.HTML,context);
    }
}
