package me.ihaq.hltv;

public final class Data {
    public static final String NAME = "HLTV News Bot";
    public static String TOKEN = "";
    public static final String RSS_LINK = "https://www.hltv.org/rss/news";

    public static final class Commands {
        public static final String START = "/start";
        public static final String STOP = "/stop";
    }

    public static final class Database {
        public static final String DRIVER = "com.mysql.cj.jdbc.Driver";
        public static final String URL = "jdbc:mysql://localhost:3306/hltv";
        public static final String USERNAME = "root";
        public static final String PASSWORD = "";
        public static final String MAX_POOL = "250";
    }
}
