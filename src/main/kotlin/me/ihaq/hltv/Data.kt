package me.ihaq.hltv

import java.util.concurrent.TimeUnit

object Data {
    val NAME = "HLTV News Bot"
    var TOKEN = ""
    val RSS_LINK = "https://www.hltv.org/rss/news"

    object Commands {
        val START = "/start"
    }

    object Database {
        val URL = "jdbc:mysql://localhost:3306/hltv"
        val USERNAME = "root"
        val PASSWORD = ""
    }

    object Schedule {
        val INITIAL_DELAY = 0
        val PERIOD = 15
        val TIME_UNIT = TimeUnit.MINUTES
    }
}