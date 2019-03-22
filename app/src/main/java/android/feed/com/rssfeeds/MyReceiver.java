package android.feed.com.rssfeeds;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by navi on 10/09/2018.
 */

public class MyReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent service = new Intent(context, ForegroundService.class);
        context.startService(service);

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
//            Intent activityIntent = new Intent(context, EmptyActivity.class);
//            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(activityIntent);
            Intent service1 = new Intent(context, ForegroundService.class);
            context.startService(service1);
        }

    }
}
