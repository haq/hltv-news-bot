package me.ihaq.hltv.news;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import me.ihaq.hltv.Bot;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class NewsManager {

    private Set<News> newsSet = new HashSet<News>();

    public void read() {
        try {
            SyndFeed feed = new SyndFeedInput().build(
                    new XmlReader(new URL(Bot.Data.RSS_LINK))
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
}
