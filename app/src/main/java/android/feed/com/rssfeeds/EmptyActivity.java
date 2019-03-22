package android.feed.com.rssfeeds;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

public class EmptyActivity extends Activity {

    private String mFeedTitle;
    private String mFeedLink;
    private String mFeedDescription;

    private List<RssFeedModel> mFeedModelList;

    String category,push_title;

    String urlLink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_empty);

        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();

        SharedPreferences sharedPreferences = getSharedPreferences("", MODE_PRIVATE);
        push_title = sharedPreferences.getString("title","");
        category = sharedPreferences.getString("category","");
        urlLink = sharedPreferences.getString("urlLink","");

        if(category.equals("New video") || category.equals("New podcast") || category.equals("New article")){

            new FetchFeedTask().execute();

        }else{
            finish();
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);

        }


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

                if(category.toLowerCase().contains("New article")){
                    mFeedModelList = parseFeedArticle(inputStream);
                }else if(category.toLowerCase().contains("New video")){
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

                if(push_title.length()>0 && category.length()>0){
                    for(int i=0;i<mFeedModelList.size();i++){
                        if(mFeedModelList.get(i).title.toString().equalsIgnoreCase(push_title.toString())){

                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mFeedModelList.get(i).link));
                            startActivity(browserIntent);



                        }
                    }
                }
                SharedPreferences.Editor editor = getSharedPreferences("", MODE_PRIVATE).edit();
                editor.putString("category", "");
                editor.putString("title", "");
                editor.commit();
                System.exit(1);
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
    @Override
    public void onBackPressed() {

        finish();
    }
    @Override
    protected void onResume() {
        super.onResume();

        int badgeCount = 0;
        SharedPreferences.Editor editor = getSharedPreferences("", MODE_PRIVATE).edit();
        editor.putInt("badgeCount", badgeCount);
        editor.commit();

        boolean success = ShortcutBadger.applyCount(EmptyActivity.this, badgeCount);
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
