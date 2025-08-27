package handler.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import handler.Update;
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
                "Olá " + user.getFirst_name() + "! Seja bem-vindo ao bot. Você foi registrado com sucesso. \n" +
                "Comandos disponíveis:\n" +
                "/adicionarDespesa <valor>* <descrição>* <categoria> | '*' -> propriedades obrigatórias\n" +
                "/listarDespesas [mês] [ano] | Exemplo: /listarDespesas 08 2023\n" +
                "/removerDespesa <id>* | '*' -> propriedade obrigatória\n" +
                "/ajuda - Mostra esta mensagem de ajuda";
        this.telegramBot.sendMessageToTelegram(update.message.chat.id, response, context);
    }
}
