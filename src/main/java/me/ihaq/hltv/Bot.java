package me.ihaq.hltv;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Bot extends TelegramLongPollingBot {

    private List<Integer> users = new ArrayList<Integer>();

    public static final class Data {
        public static final String NAME = "HLTV News Bot";
        public static final String TOKEN = "";
        public static final String RSS_LINK = "https://www.hltv.org/rss/news";

        public static final class Commands {
            public static final String START = "/start";
            public static final String STOP = "/stop";
        }
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

            if (text.equals(Data.Commands.START)) {
                // add user to list
            } else if (text.equals(Data.Commands.STOP)) {
                // remover user from the list
            }
        }
    }

    public void readNews() {
        try {
            SyndFeed feed = new SyndFeedInput().build(
                    new XmlReader(new URL(Data.RSS_LINK))
            );

            for (SyndEntry entry : feed.getEntries()) {
                System.out.println(entry.getTitle());
                System.out.println(entry.getDescription().getValue());
                System.out.println(entry.getLink());
                System.out.println(entry.getPublishedDate());
                System.out.println();
            }
        } catch (FeedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getBotUsername() {
        return Data.NAME;
    }

    public String getBotToken() {
        return Data.TOKEN;
    }
}
