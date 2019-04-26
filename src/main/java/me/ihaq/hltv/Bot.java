package me.ihaq.hltv;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Bot extends TelegramLongPollingBot {

    private HikariDataSource dataSource;

    public static void main(String[] args) {
        Data.TOKEN = args[0];

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(Data.Database.URL);
        dataSource.setUsername(Data.Database.USERNAME);
        dataSource.setPassword(Data.Database.PASSWORD);
        dataSource.addDataSourceProperty("cachePrepStmts", "true");
        dataSource.addDataSourceProperty("prepStmtCacheSize", "250");
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(
                    new Bot(dataSource, scheduler)
            );
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            dataSource.close();
            scheduler.shutdown();
        }));
    }

    private Bot(HikariDataSource dataSource, ScheduledExecutorService scheduler) {
        this.dataSource = dataSource;

        scheduler.scheduleAtFixedRate(() -> {
            try (Connection connection = dataSource.getConnection()) {
                SyndFeed feed = new SyndFeedInput().build(
                        new XmlReader(new URL(Data.RSS_LINK))
                );

                long now = new Date().getTime();
                ResultSet result = connection.createStatement().executeQuery("SELECT * FROM chats");

                for (SyndEntry entry : feed.getEntries()) {
                    long previous = entry.getPublishedDate().getTime();
                    long max = TimeUnit.MILLISECONDS.convert(
                            Data.Schedule.PERIOD, Data.Schedule.TIME_UNIT
                    );

                    if (now - previous <= max) {
                        while (result.next()) {
                            String chatId = result.getString("chat_id");
                            try {
                                SendMessage message = new SendMessage()
                                        .setChatId(chatId)
                                        .setText(entry.getLink());
                                execute(message);
                            } catch (TelegramApiException e) {
                                if (e.toString().contains("bot was blocked by the user")) { // check if a user has blocked the bot
                                    removeChat(chatId);
                                }
                            }
                            Thread.sleep(250);
                        }
                        result.beforeFirst();
                    }
                }
            } catch (FeedException | IOException | SQLException | InterruptedException e) {
                e.printStackTrace();
            }
        }, Data.Schedule.INITIAL_DELAY, Data.Schedule.PERIOD, Data.Schedule.TIME_UNIT);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) {
            return;
        }

        Message message = update.getMessage();
        String chatId = message.getChatId().toString();

        if (!message.getNewChatMembers().isEmpty()) { // we joined a group chat
            message.getNewChatMembers()
                    .stream()
                    .filter(User::getBot)
                    .filter(user -> user.getFirstName().equalsIgnoreCase(Data.NAME))
                    .forEach(user -> addChat(chatId));
        } else if (message.getLeftChatMember() != null) { // we left a group chat
            User leftUser = message.getLeftChatMember();
            if (leftUser.getBot() && leftUser.getFirstName().equalsIgnoreCase(Data.NAME)) {
                removeChat(chatId);
            }
        } else if (message.hasText() && message.getText().equals(Data.Commands.START)) { // someone started conversation with bot
            addChat(chatId);
        }
    }

    @Override
    public String getBotUsername() {
        return Data.NAME;
    }

    @Override
    public String getBotToken() {
        return Data.TOKEN;
    }

    private void execute(String sql) {
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement(sql).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addChat(String chatId) {
        execute(
                String.format(
                        "INSERT INTO chats (chat_id) VALUES ('%s')", chatId
                )
        );
    }

    private void removeChat(String chatId) {
        execute(
                String.format(
                        "DELETE FROM chats WHERE chat_id='%s'", chatId
                )
        );
    }

}
