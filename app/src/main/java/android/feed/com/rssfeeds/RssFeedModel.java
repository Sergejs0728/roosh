package android.feed.com.rssfeeds;

/**
 * Created by navi on 17/06/2018.
 */

public class RssFeedModel {

    public String title;
    public String link;
    public String pubDate;
    public String imgURL;

    public RssFeedModel(String title, String link, String pubDate,String imgURL) {
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.imgURL = imgURL;

    }
}