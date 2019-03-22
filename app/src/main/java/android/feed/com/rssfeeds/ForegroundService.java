package android.feed.com.rssfeeds;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.feed.com.rssfeeds.R;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import me.leolin.shortcutbadger.ShortcutBadger;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by admin on 08/09/2017.
 */

public class ForegroundService extends Service{


    private String mFeedTitle;
    private String mFeedLink;
    private String mFeedDescription;


    private static final String LOG_TAG = "ForegroundService";
    private Runnable runnable;
    OkHttpClient client;

    ArrayList<String> feedArray;

    List<RssFeedModel> mFeedModelList;
    List<String> articleArray,videoArray,podcastArray;
    ArrayList<String> articleTitleArray = new ArrayList<String>(),videoTitleArray = new ArrayList<String>(),podvastTitleArray = new ArrayList<String>();
    int seconds = 0;
    String urlLink = "",category;
    private Handler handler = new Handler();
    private static ForegroundService instance;

    AppController appController = AppController.getInstance();

    public ForegroundService() {
        client = new OkHttpClient();
    }

    public static ForegroundService getInstance() {
        if (instance == null) {
            instance = new ForegroundService();
        }
        return instance;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {



        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {

                //Perform background work here

                handler.post(new Runnable() {
                    public void run() {
                        //Perform GUI updation work here
                        //Toast work also

                        seconds = seconds + 1;

                        if(seconds % 3 == 0){
                            urlLink = "https://www.rooshvforum.com/syndication.php?limit=25";
                            category = "New article";
                        }else if (seconds % 3 == 1){
                            urlLink = "https://www.youtube.com/feeds/videos.xml?channel_id=UC16j6EppP0K85CzYMduNCqw";
                            category = "New video";
                        } else{
                            urlLink = "http://feeds.soundcloud.com/users/soundcloud:users:253651639/sounds.rss";
                            category = "New podcast";
                        }
                        seconds = seconds % 180;
                        Toast.makeText(ForegroundService.this, "Running!", Toast.LENGTH_LONG).show();
//                        if(seconds == 0){
//                            showNotification("test","test","test");
//                        }
                        new FetchFeedTask().execute();


                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 600000);

//1200000
        return START_STICKY;
    }
    private void showNotification(String category,String noti_title,String noti_url) {
//        Intent browserIntent = new Intent(ForegroundService.this, ShowURLActivity.class);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(noti_url));


        PendingIntent contentIntent = PendingIntent.getActivity(ForegroundService.this, 0, browserIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder b = new NotificationCompat.Builder(ForegroundService.this);

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.headonly)
                .setTicker("Hearty365")
                .setContentTitle(category)
                .setContentText(noti_title)
                .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .setContentInfo("Info");




        final int min = 1;
        final int max = 8000;
        final int random = new Random().nextInt((max - min) + 1) + min;

        NotificationManager notificationManager = (NotificationManager) ForegroundService.this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(random, b.build());

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy");
        Toast.makeText(this, "Service Detroyed!", Toast.LENGTH_SHORT).show();
    }

    class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {



        @Override
        protected void onPreExecute() {
//        mSwipeLayout.setRefreshing(true);
//        urlLink = mEditText.getText().toString();



        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (TextUtils.isEmpty(urlLink))
                return false;

            try {
                if(!urlLink.startsWith("http://") && !urlLink.startsWith("https://"))
                    urlLink = "http://" + urlLink;

                URL url = new URL(urlLink);
                InputStream inputStream = url.openConnection().getInputStream();


                if(category.toLowerCase().contains("new article")){
                    mFeedModelList = parseFeedArticle(inputStream);
                }else if(category.toLowerCase().contains("new video")){
                    mFeedModelList = parseFeedVideo(inputStream);
                }else{
                    mFeedModelList = parseFeedPodcast(inputStream);
                }


                return true;
            } catch (IOException e) {
                Log.e("", "Error", e);
            } catch (XmlPullParserException e) {
                Log.e("", "Error", e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {

            if (success) {

                SharedPreferences prefs = getSharedPreferences("", MODE_PRIVATE);
                boolean isOn = prefs.getBoolean(category,true);

                List<RssFeedModel> previous;

                if(mFeedModelList.size() == 0){
                    return;
                }

                if(category.equals("New article")){

                    SharedPreferences sharedPreferences = getSharedPreferences("", MODE_PRIVATE);
                    int cnt = sharedPreferences.getInt("cnt_article",0);

                    if(cnt == 0){

                        for(int i=0;i<mFeedModelList.size();i++){

                            articleTitleArray.add(mFeedModelList.get(i).title);
                        }
                        saveArray(articleTitleArray,"articleArray",ForegroundService.this);

                        SharedPreferences.Editor editor = getSharedPreferences("", MODE_PRIVATE).edit();
                        editor.putInt("cnt_article", mFeedModelList.size());
                        editor.commit();

                        return;
                    }
                    if(mFeedModelList.size() == 0){
                        return;
                    }

//                    if(!isOn){
//                        return;
//                    }
//                    ArrayList<String> articleList = loadArray("articleArray",ForegroundService.this);
//
//                    for(int i=0;i<mFeedModelList.size()-1;i++){
//                        for(int j=0;i<articleList.size()-1;j++){
//
//
//                            if(mFeedModelList.get(i).title.equals(articleList.get(j))){
//
//                            }else{
//                                showNotification(category,mFeedModelList.get(i).title,mFeedModelList.get(i).link);
//                            }
//                        }
//                    }
//                    for(int i=0;i<mFeedModelList.size()-1;i++){
//
//                        feedArray.add(mFeedModelList.get(i).title);
//                    }
//                    saveArray(feedArray,"articleArray",ForegroundService.this);


                    if(cnt == mFeedModelList.size()){


                        articleArray = loadArray("articleArray",ForegroundService.this);
                        if(!isOn){

                            return;
                        }
//                        Toast.makeText(ForegroundService.this, String.valueOf(cnt), Toast.LENGTH_SHORT).show();

                        for(int i = 0;i<mFeedModelList.size();i++){
                            if(articleArray.contains(mFeedModelList.get(i).title)){

                            }else{
                                showNotification(category,mFeedModelList.get(i).title,mFeedModelList.get(i).link);
                            }
                        }

                        for(int i=0;i<mFeedModelList.size();i++){

                            articleTitleArray.add(mFeedModelList.get(i).title);
                        }
                        saveArray(articleTitleArray,"articleArray",ForegroundService.this);


                    }





                }else if(category.equals("New video")){

                    SharedPreferences sharedPreferences = getSharedPreferences("", MODE_PRIVATE);
                    int cnt = sharedPreferences.getInt("cnt_video",0);
                    if(cnt == 0){

                        for(int i=0;i<mFeedModelList.size();i++){

                            videoTitleArray.add(mFeedModelList.get(i).title);
                        }
                        saveArray(videoTitleArray,"videoArray",ForegroundService.this);

                        SharedPreferences.Editor editor = getSharedPreferences("", MODE_PRIVATE).edit();
                        editor.putInt("cnt_video", mFeedModelList.size());
                        editor.commit();

                        return;
                    }
                    if(mFeedModelList.size() == 0){
                        return;
                    }


                    if(cnt == mFeedModelList.size()){

                        videoArray = loadArray("videoArray",ForegroundService.this);
                        if(!isOn){

                            return;
                        }
                        for(int i = 0;i<mFeedModelList.size();i++){
                            if(videoArray.contains(mFeedModelList.get(i).title)){

                            }else{
                                showNotification(category,mFeedModelList.get(i).title,mFeedModelList.get(i).link);
                            }
                        }

                        for(int i=0;i<mFeedModelList.size();i++){

                            videoTitleArray.add(mFeedModelList.get(i).title);
                        }
                        saveArray(videoTitleArray,"videoArray",ForegroundService.this);

                    }



                }else{


                    SharedPreferences sharedPreferences = getSharedPreferences("", MODE_PRIVATE);
                    int cnt = sharedPreferences.getInt("cnt_podcast",0);
                    if(cnt == 0){

                        for(int i=0;i<mFeedModelList.size();i++){

                            podvastTitleArray.add(mFeedModelList.get(i).title);
                        }
                        saveArray(podvastTitleArray,"podcastArray",ForegroundService.this);

                        SharedPreferences.Editor editor = getSharedPreferences("", MODE_PRIVATE).edit();
                        editor.putInt("cnt_podcast", mFeedModelList.size());
                        editor.commit();

                        return;
                    }

                    if(mFeedModelList.size() == 0){
                        return;
                    }

                    if(cnt == mFeedModelList.size()){


                        podcastArray = loadArray("podcastArray",ForegroundService.this);
                        if(!isOn){

                            return;
                        }
                        for(int i = 0;i<mFeedModelList.size();i++){
                            if(podcastArray.contains(mFeedModelList.get(i).title)){
                                return;
                            }else{
                                showNotification(category,mFeedModelList.get(i).title,mFeedModelList.get(i).link);
                            }
                        }

                        for(int i=0;i<mFeedModelList.size();i++){

                            podvastTitleArray.add(mFeedModelList.get(i).title);
                        }
                        saveArray(podvastTitleArray,"podcastArray",ForegroundService.this);
                    }



                }
//                System.exit(1);
//                if(isnotification){
//                    adapterNotification = new AdapterNotification(titleArrary,textArray,dateArrary);
//                    listView.setAdapter(adapterNotification);
//                    adapterNotification.notifyDataSetChanged();
//                }else{
//                    adapterDetails = new AdapterDetails(mFeedModelList,isnotification);
//                    listView.setAdapter(adapterDetails);
//                    adapterDetails.notifyDataSetChanged();
//                }



            } else {
//                Toast.makeText(getApplicationContext(),
//                        "Enter a valid Rss feed url",
//                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public ArrayList<String> loadArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("", 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        ArrayList<String> array = new ArrayList<String>();
        array.clear();
        for(int i=0;i<size;i++)
            array.add(prefs.getString(arrayName + "_" + i, null));
        return array;
    }
    public boolean saveArray(ArrayList<String> array, String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName +"_size", array.size());
        for(int i=0;i<array.size();i++)
            editor.putString(arrayName + "_" + i, array.get(i));
        return editor.commit();
    }
    public List<RssFeedModel> parseFeedArticle(InputStream inputStream) throws XmlPullParserException,
            IOException {
        String title = null;
        String link = null;
        String pubDate = null;
        String content = null;
        boolean isItem = false;
        boolean flag = false;
        List<RssFeedModel> items = new ArrayList<>();
        List<String> hrArray = new ArrayList<>();

        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                int eventType = xmlPullParser.getEventType();

                String name = xmlPullParser.getName();
                if(name == null)
                    continue;

                if(eventType == XmlPullParser.END_TAG) {
                    if(name.equalsIgnoreCase("channel")) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if(name.equalsIgnoreCase("channel")) {
                        isItem = true;
                        continue;
                    }
                }
                if(eventType == XmlPullParser.END_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = true;
                        continue;
                    }
                }

                Log.d("MyXmlParser", "Parsing name ==> " + name);
                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }

                if (name.equalsIgnoreCase("link")) {
                    link = result;
                } else if (name.equalsIgnoreCase("comments")) {
                    if(flag == false){
                        pubDate = null;
                    }else{
                        pubDate = result;
                    }
                    flag = true;
                }
                else if (name.equalsIgnoreCase("pubDate")) {
                    pubDate = result;
                }
                else if (name.equalsIgnoreCase("link")) {
                    link = result;
                }
                else if (name.equalsIgnoreCase("title")) {
                    title = result;
                } else if (name.equalsIgnoreCase("content:encoded")) {
                    content = result;
                }

                if (title != null && link != null && pubDate != null && content != null) {
                    if(isItem) {
                        String imgUrl = Jsoup.parse(content).select("img").attr("src");
                        RssFeedModel item = new RssFeedModel(title, link, pubDate,imgUrl);
                        items.add(item);

//                        for(int i=0;i<Jsoup.parse(content).select("h3").size();i++){
//                            String hr = Jsoup.parse(content).select("h3").get(i).toString();
//                            hr = hr.replace("<h3>","");
//                            hr = hr.replace("</h3>","");
//                            hrArray.add(hr);
//                        }
//                        for(int i=0;i<Jsoup.parse(content).select("p").select("img").size();i++){
//                            RssFeedModel item1 = new RssFeedModel(hrArray.get(i), "", "",Jsoup.parse(content).select("p").select("img").get(i).attr("src"));
//                            items.add(item1);
//                        }
//                        String subTitle = Jsoup.parse(content).select("hr").attr("src");

                    }
                    else {
                        mFeedTitle = title;
                        mFeedLink = link;
                        mFeedDescription = pubDate;
                    }

                    title = null;
                    link = null;
                    pubDate = null;
                    content = null;
                    isItem = false;
                }
            }

            return items;
        } finally {
            inputStream.close();
        }
    }

    public List<RssFeedModel> parseFeedVideo(InputStream inputStream) throws XmlPullParserException,
            IOException {
        String title = null;
        String link = null;
        String pubDate = null;
        String imgURL = null;
        boolean isItem = false;
        boolean flag = false;
        List<RssFeedModel> items = new ArrayList<>();
        List<String> hrArray = new ArrayList<>();

        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                int eventType = xmlPullParser.getEventType();

                String name = xmlPullParser.getName();
                if(name == null)
                    continue;

                if(eventType == XmlPullParser.END_TAG) {
                    if(name.equalsIgnoreCase("entry")) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if(name.equalsIgnoreCase("entry")) {
                        isItem = true;
                        continue;
                    }
                }
                Log.d("MyXmlParser", "Parsing name ==> " + name);
                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }

                if (name.equalsIgnoreCase("link")) {
                    link = xmlPullParser.getAttributeValue(null, "href");

                }
                else if (name.equalsIgnoreCase("published")) {
                    pubDate = result;
                }
                else if (name.equalsIgnoreCase("title")) {
                    title = result;
                }
                else if (name.equalsIgnoreCase("media:thumbnail")) {
                    imgURL = xmlPullParser.getAttributeValue(null, "url");
                }

                if (title != null && link != null && pubDate != null && imgURL != null) {
                    if(isItem) {
                        RssFeedModel item = new RssFeedModel(title, link, pubDate,imgURL);
                        items.add(item);


                    }
                    else {
                        mFeedTitle = title;
                        mFeedLink = link;
                        mFeedDescription = pubDate;
                    }

                    title = null;
                    link = null;
                    pubDate = null;
                    imgURL = null;
                    isItem = false;
                }
            }

            return items;
        } finally {
            inputStream.close();
        }
    }
    public List<RssFeedModel> parseFeedPodcast(InputStream inputStream) throws XmlPullParserException,
            IOException {
        String title = null;
        String link = null;
        String pubDate = null;
        String imgURL = null;
        boolean isItem = false;
        boolean flag = false;
        List<RssFeedModel> items = new ArrayList<>();
        List<String> hrArray = new ArrayList<>();

        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                int eventType = xmlPullParser.getEventType();

                String name = xmlPullParser.getName();
                if(name == null)
                    continue;

                if(eventType == XmlPullParser.END_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = true;
                        continue;
                    }
                }
                Log.d("MyXmlParser", "Parsing name ==> " + name);
                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }

                if (name.equalsIgnoreCase("link")) {
                    link = result;

                }
                else if (name.equalsIgnoreCase("pubDate")) {
                    pubDate = result;
                }
                else if (name.equalsIgnoreCase("title")) {
                    title = result;
                }
                else if (name.equalsIgnoreCase("itunes:image")) {
                    imgURL = xmlPullParser.getAttributeValue(null, "href");
                }

                if (title != null && link != null && pubDate != null && imgURL != null) {
                    if(isItem) {
                        RssFeedModel item = new RssFeedModel(title, link, pubDate,imgURL);
                        items.add(item);


                    }
                    else {
                        mFeedTitle = title;
                        mFeedLink = link;
                        mFeedDescription = pubDate;
                    }

                    title = null;
                    link = null;
                    pubDate = null;
                    imgURL = null;
                    isItem = false;
                }
            }

            return items;
        } finally {
            inputStream.close();
        }
    }


}
