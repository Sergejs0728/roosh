package android.feed.com.rssfeeds;

import android.app.Application;

import java.util.List;

/**
 * Created by haha on 19/12/2017.
 */

public class AppController extends Application {
    private static List<RssFeedModel> articleFeedModelList;
    private static List<RssFeedModel> videoFeedModelList;
    private static List<RssFeedModel> podcastFeedModelList;
    private static  AppController instance;

    private AppController(){

    }
    public void setArticleFeedModelList(List<RssFeedModel> t){
        AppController.articleFeedModelList = t;

    }

    public List<RssFeedModel> getArticleFeedModelList(){
        return AppController.articleFeedModelList;
    }

    public void setVideoFeedModelList(List<RssFeedModel> t){
        AppController.articleFeedModelList = t;

    }

    public List<RssFeedModel> getVideoFeedModelList(){
        return AppController.videoFeedModelList;
    }

    public void setPodcastFeedModelList(List<RssFeedModel> t){
        AppController.podcastFeedModelList = t;

    }

    public List<RssFeedModel> getPodcastFeedModelList(){
        return AppController.podcastFeedModelList;
    }

    public static synchronized AppController getInstance(){
        if(instance == null){
            instance = new AppController();
        }
        return instance;
    }
}
