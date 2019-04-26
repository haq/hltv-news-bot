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
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Bot extends TelegramLongPollingBot {

    private Database database;

    public static void main(String[] args) {
        Data.TOKEN = args[0];

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        ApiContextInitializer.init();

        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(
                    new Bot(new Database(), scheduler)
            );
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        scheduler.shutdown();
    }

    private Bot(Database database, ScheduledExecutorService scheduler) {
        this.database = database;

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    SyndFeed feed = new SyndFeedInput().build(
                            new XmlReader(new URL(Data.RSS_LINK))
                    );

                    for (SyndEntry entry : feed.getEntries()) {
                      /*  if (entry.getPublishedDate().compareTo()) {

                        }*/

               /* System.out.println(entry.getTitle());
                System.out.println(entry.getDescription().getValue());
                System.out.println(entry.getLink());
                System.out.println();
                System.out.println();*/
                    }
                } catch (FeedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }, Data.Schedule.INITIAL_DELAY, Data.Schedule.PERIOD, Data.Schedule.TIME_UNIT);
    }

    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            Message message = update.getMessage();
            String text = message.getText();
            long chatId = message.getChatId();

            if (text.equals(Data.Commands.START)) {
                try {
                    database.connect().prepareStatement(
                            String.format(
                                    "INSERT INTO users (chat_id, first_name, last_name) VALUES (%d, '%s', '%s')",
                                    chatId,
                                    message.getFrom().getFirstName(),
                                    message.getFrom().getLastName()
                            )
                    ).execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    database.disconnect();
                }
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
