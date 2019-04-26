package me.ihaq.hltv;

import java.util.concurrent.TimeUnit;

public final class Data {
    public static final String NAME = "HLTV News Bot";
    public static String TOKEN = "";
    public static final String RSS_LINK = "https://www.hltv.org/rss/news";

    public static final class Commands {
        public static final String START = "/start";
    }

    public static final class Database {
        public static final String DRIVER = "com.mysql.cj.jdbc.Driver";
        public static final String URL = "jdbc:mysql://localhost:3306/hltv";
        public static final String USERNAME = "root";
        public static final String PASSWORD = "";
        public static final String MAX_POOL = "250";
    }

    public static final class Schedule {
        public static final int INITIAL_DELAY = 0;
        public static final int PERIOD = 10;
        public static final TimeUnit TIME_UNIT = TimeUnit.MINUTES;
    }
}
