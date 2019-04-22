package me.ihaq.hltv;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;

public class Bot extends TelegramLongPollingBot {

    public static class Data {
        public static final String NAME = "HLTV News Bot";
        public static final String TOKEN = "";
        public static final String RSS_LINK = "https://www.hltv.org/rss/news";
    }

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new Bot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            Message message = update.getMessage();
            String text = message.getText();
            long chatId = message.getChatId();

            try {
                execute(
                        new SendMessage()
                                .setChatId(chatId)
                                .setText("test")
                                .enableMarkdown(true)
                );
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public String getBotUsername() {
        return Data.NAME;
    }

    public String getBotToken() {
        return Data.TOKEN;
    }
}
