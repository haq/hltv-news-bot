package me.ihaq.hltv

import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import com.zaxxer.hikari.HikariDataSource
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.net.URL
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    Data.TOKEN = args[0]

    val scheduler = Executors.newSingleThreadScheduledExecutor()
    val dataSource = HikariDataSource().apply {
        this.jdbcUrl = Data.Database.URL
        this.username = Data.Database.USERNAME
        this.password = Data.Database.PASSWORD
        this.addDataSourceProperty("serverTimezone", "UTC")
    }

    ApiContextInitializer.init()
    val telegramBotsApi = TelegramBotsApi()
    try {
        telegramBotsApi.registerBot(
            Bot(dataSource, scheduler)
        )
    } catch (e: TelegramApiException) {
        e.printStackTrace()
    }

    Runtime.getRuntime().addShutdownHook(Thread {
        dataSource.close()
        scheduler.shutdown()
    })
}

class Bot(private val dataSource: HikariDataSource, scheduler: ScheduledExecutorService) : TelegramLongPollingBot() {

    init {
        scheduler.scheduleAtFixedRate({
            dataSource.connection.use { connection ->
                val feed = SyndFeedInput().build(
                    XmlReader(URL(Data.RSS_LINK))
                )

                val now = Date().time
                val result = connection.createStatement().executeQuery("SELECT * FROM chats")

                feed.entries.forEach { entry ->
                    val previous = entry.publishedDate.time
                    val max = TimeUnit.MILLISECONDS.convert(
                        Data.Schedule.PERIOD.toLong(), Data.Schedule.TIME_UNIT
                    )

                    if (now - previous <= max) {
                        while (result.next()) {
                            val chatId = result.getString("chat_id")
                            try {
                                execute<Message, SendMessage>(
                                    SendMessage().apply {
                                        this.chatId = chatId
                                        this.text = entry.link
                                    }
                                )
                            } catch (e: TelegramApiException) {
                                if (e.toString().contains("bot was blocked by the user")) { // check if a user has blocked the bot
                                    modifyChatTable(chatId, true)
                                }
                            }

                            Thread.sleep(250)
                        }
                        result.beforeFirst()
                    }
                }
            }

        }, Data.Schedule.INITIAL_DELAY.toLong(), Data.Schedule.PERIOD.toLong(), Data.Schedule.TIME_UNIT)
    }

    override fun onUpdateReceived(update: Update) {
        if (!update.hasMessage()) {
            return
        }

        val message = update.message
        val chatId = message.chatId.toString()

        if (!message.newChatMembers.isEmpty()) { // bot joined group chat
            val validBots = message.newChatMembers
                .filter { it.bot }
                .filter { it.firstName.equals(Data.NAME, ignoreCase = true) }
            repeat(validBots.size) { modifyChatTable(chatId) }
        } else if (message.leftChatMember != null) { // bot left group chat
            val leftUser = message.leftChatMember
            if (leftUser.bot && leftUser.firstName.equals(Data.NAME, ignoreCase = true)) {
                modifyChatTable(chatId, true)
            }
        } else if (message.hasText() && message.text == Data.Commands.START) { // someone started conversation with bot
            modifyChatTable(chatId)
        }
    }

    override fun getBotUsername() = Data.NAME

    override fun getBotToken() = Data.TOKEN

    private fun execute(sql: String) =
        dataSource.connection.use { connection -> connection.prepareStatement(sql).execute() }

    private fun modifyChatTable(chatId: String, remove: Boolean = false) {
        val sql = if (!remove) {
            "INSERT INTO chats (chat_id) VALUES ('$chatId')"
        } else {
            "DELETE FROM chats WHERE chat_id='$chatId'"
        }
        execute(sql)
    }

}
