package mobi.esys.rss;

import android.util.Log;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import nl.matshofman.saxrssreader.RssFeed;
import nl.matshofman.saxrssreader.RssItem;
import nl.matshofman.saxrssreader.RssReader;

/**
 * Created by Артем on 02.03.2015.
 */
public class RSS {
    private static final String TAG = "RSS";

    public static String parseRSSURL(URL url, String divider) {
        StringBuilder rssFeed = new StringBuilder();
        try {
            RssFeed feed = RssReader.read(url);
            List<RssItem> rssItems = feed.getRssItems();
            if (rssItems.size() > 0) {
                for (RssItem rssItem : rssItems) {
                    Log.i(TAG, "rss item: " + rssItem.getTitle());
                    rssFeed.append(rssItem.getTitle()).append(" ").append(divider).append(" ");
                }
            } else {
                rssFeed.append("");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rssFeed.toString();
    }

}
