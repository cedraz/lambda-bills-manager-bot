package handler.handlers;

import handler.telegram.TelegramBot;

public class HelpHandler extends BaseHandler {
    public HelpHandler(TelegramBot telegramBot) {
        super(telegramBot);
    }

    @Override
    public void handle(handler.Update update, com.amazonaws.services.lambda.runtime.Context context) {
        long chatId = update.message.chat.id;
        String helpMessage = "Comandos disponíveis:\n" +
                "/adicionarDespesa <valor>* <descrição>* <categoria> | '*' -> propriedades obrigatórias\n" +
                "/listarDespesas [mês] [ano] | Exemplo: /listarDespesas 08 2023\n" +
                "/removerDespesa <id>* | '*' -> propriedade obrigatória\n" +
                "/ajuda - Mostra esta mensagem de ajuda";
        this.telegramBot.sendMessageToTelegram(chatId, helpMessage, context);
    }
}
