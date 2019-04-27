package me.ihaq.hltv

import java.util.concurrent.TimeUnit

object Data {
    lateinit var TOKEN: String

    const val NAME = "HLTV News Bot"
    const val RSS_LINK = "https://www.hltv.org/rss/news"

    object Commands {
        const val START = "/start"
    }

    object Database {
        const val URL = "jdbc:mysql://localhost:3306/hltv"
        const val USERNAME = "root"
        const val PASSWORD = ""
    }

    object Schedule {
        const val INITIAL_DELAY = 0
        const val PERIOD = 15
        val TIME_UNIT = TimeUnit.MINUTES
    }
}