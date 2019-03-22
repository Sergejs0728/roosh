package android.feed.com.rssfeeds.fcm;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.feed.com.rssfeeds.AdapterFeeds;
import android.feed.com.rssfeeds.EmptyActivity;
import android.feed.com.rssfeeds.MainActivity;
import android.feed.com.rssfeeds.R;
import android.feed.com.rssfeeds.RssFeedModel;
import android.feed.com.rssfeeds.VideoFragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jsoup.Jsoup;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by Pavel on 10/16/2017.
 */

public class MessagingService extends FirebaseMessagingService {


    String urlLink;

    boolean isOn;
    String category,push_title;
    private static final String TAG = "FCM";



    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
//        if (AppConfig.DEBUG) {
//            Log.d(TAG, "From: " + remoteMessage.getFrom());
//
//        }

        SharedPreferences prefs = getSharedPreferences("", MODE_PRIVATE);
        isOn = prefs.getBoolean("isOn",true);
        if(!isOn){

            return;
        }

        if (remoteMessage.getNotification() != null && remoteMessage.getNotification().getBody() != null) {
//            createNotification(remoteMessage);
            super.onMessageReceived(remoteMessage);
        } else {
            super.onMessageReceived(remoteMessage);
        }
        if(isAppIsInBackground(this)){

            int badgeCount = prefs.getInt("badgeCount", 0);

            badgeCount +=1;


            SharedPreferences.Editor editor = getSharedPreferences("", MODE_PRIVATE).edit();
            editor.putInt("badgeCount", badgeCount);
            editor.commit();

            try {

            } catch (NumberFormatException e) {
//                Toast.makeText(getApplicationContext(), "Error input", Toast.LENGTH_SHORT).show();
            }

            boolean success = ShortcutBadger.applyCount(getApplicationContext(), badgeCount);
        }



    }
    public void handleIntent(Intent intent)
    {

        push_title = intent.getExtras().get("gcm.notification.body").toString();


        if(intent.getExtras().get("google.c.a.c_l") != null){
            category = intent.getExtras().get("google.c.a.c_l").toString();
        }else{
            category = "";
        }

        if(category.toLowerCase().contains("New article")){
            urlLink = "https://www.rooshvforum.com/syndication.php?limit=25";
            category = "New article";
        }else if(category.toLowerCase().contains("New video")){
            urlLink = "https://www.youtube.com/feeds/videos.xml?channel_id=UC16j6EppP0K85CzYMduNCqw";
            category = "New video";
        }else{
            urlLink = "http://feeds.soundcloud.com/users/soundcloud:users:253651639/sounds.rss";
            category = "New podcast";
        }




        SharedPreferences prefs = getSharedPreferences("", MODE_PRIVATE);
        isOn = prefs.getBoolean(category,true);

        if(isOn){

            SharedPreferences.Editor editor = getSharedPreferences("", MODE_PRIVATE).edit();
            editor.putString("category", category);
            editor.putString("title", push_title);
            editor.putString("urlLink", urlLink);
            editor.commit();

            try
            {
                RemoteMessage.Builder builder = new RemoteMessage.Builder("MessagingService");

                onMessageReceived(builder.build());
                super.handleIntent(intent);
            }
            catch (Exception e)
            {
                super.handleIntent(intent);
            }
        }

    }


    private void createNotification(RemoteMessage remoteMessage) {
        Context context = getApplicationContext();


        Intent intent = new Intent(this, EmptyActivity.class);
//        intent.putExtra("isPushnotification",remoteMessage.getData().values().toArray()[0].toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder ncomp = new NotificationCompat.Builder(context);
        ncomp.setContentTitle(context.getResources().getString(R.string.app_name));
        ncomp.setContentText(remoteMessage.getNotification().getBody());
        ncomp.setTicker(remoteMessage.getNotification().getBody());
        ncomp.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        ncomp.setSmallIcon(R.drawable.headonly);
        ncomp.setAutoCancel(true);
        ncomp.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody()));
        ncomp.setContentIntent(pendingIntent);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ncomp.setChannelId(context.getString(R.string.default_notification_channel_id));

            NotificationChannel channel = new NotificationChannel(context.getString(R.string.default_notification_channel_id),
                    context.getResources().getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT);

            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = ncomp.build();
        notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE |
                Notification.DEFAULT_SOUND;
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        int notificationId = R.string.app_name + ((int) (Math.random() * 100000));
        notificationManager.notify(notificationId, notification);

    }


    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }
        return isInBackground;
    }
}