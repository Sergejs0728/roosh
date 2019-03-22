package android.feed.com.rssfeeds;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

public class MainActivity extends AppCompatActivity {


    int index = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Intent service = new Intent(this, MyReceiver.class);
//        startService(service);
        scheduleNotification();
        // Title Bar Remove
//        getSupportActionBar().hide();

//        SharedPreferences sharedPreferences = getSharedPreferences("", MODE_PRIVATE);
//        String title = sharedPreferences.getString("title","");
//        String category = sharedPreferences.getString("category","");
//
//
//        if(title.length()>0 && category.length()>0){
//            if(category.toLowerCase().equals("new video")){
//
//                index = 1;
//                loadFragment(new VideoFragment());
//                getSupportActionBar().setTitle("Videos");
//            }else if(category.toLowerCase().equals("newodcasts")){
//                index = 2;
//                loadFragment(new PodcastFragment());
//                getSupportActionBar().setTitle("Podcasts");
//            }else{
//                index = 0;
//                loadFragment(new ArticleFragment());
//                getSupportActionBar().setTitle("Articles");
//            }
//        }else{
//            index = 0;
//            loadFragment(new ArticleFragment());
//            getSupportActionBar().setTitle("Articles");
//        }

        index = 0;
        loadFragment(new ArticleFragment());
        getSupportActionBar().setTitle("Articles");

        final BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx)findViewById(R.id.bottom_navigation);
        // BottomNavigationViewEx

        bottomNavigationViewEx.enableAnimation(true);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);



        final List<String> lstTitle = new ArrayList<String>();
        lstTitle.add("Articles");
        lstTitle.add("Videos");
        lstTitle.add("Podcasts");
        lstTitle.add("Settings");

//        Intent service = new Intent(MainActivity.this, ForegroundService.class);
//        service.setAction("com.marothiatechs.foregroundservice.action.startforeground");
//        startService(service);

        for(int i=0;i<4;i++){
            if(i==index){

                bottomNavigationViewEx.setIconTintList(i,ColorStateList.valueOf(Color.WHITE));
                bottomNavigationViewEx.setTextTintList(i,ColorStateList.valueOf(Color.WHITE));;
            }else{
                bottomNavigationViewEx.setIconTintList(i,ColorStateList.valueOf(Color.LTGRAY));
                bottomNavigationViewEx.setTextTintList(i,ColorStateList.valueOf(Color.LTGRAY));
            }
        }

        // Set Nav Font
        bottomNavigationViewEx.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Fragment fragments[] = {new ArticleFragment(),new VideoFragment(),new PodcastFragment(),new SettingFragment()};

                int position = bottomNavigationViewEx.getMenuItemPosition(item);

                loadFragment(fragments[position]);

                for(int i=0;i<4;i++){
                    if(i==position){
                        getSupportActionBar().setTitle(lstTitle.get(i));
                        bottomNavigationViewEx.setIconTintList(i,ColorStateList.valueOf(Color.WHITE));
                        bottomNavigationViewEx.setTextTintList(i,ColorStateList.valueOf(Color.WHITE));

                    }else{

                        bottomNavigationViewEx.setIconTintList(i,ColorStateList.valueOf(Color.LTGRAY));
                        bottomNavigationViewEx.setTextTintList(i,ColorStateList.valueOf(Color.LTGRAY));
                    }
                }

                return false;
            }
        });




    }

    private void scheduleNotification() {

        Intent notificationIntent = new Intent(this, MyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, 0);

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
//        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1, pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1, pendingIntent);
    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        System.exit(1);
    }
}
