package com.spambytes.prodstore;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class ReminderBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        /*Intent resultIntent = new Intent(context, LandingActivity.class);
        intent.putExtra("info", (int) System.currentTimeMillis());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent((int) System.currentTimeMillis(), PendingIntent.FLAG_UPDATE_CURRENT);*/

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notifyUs")
                .setSmallIcon(R.drawable.ic_save_icon)
                .setContentTitle("Hello there")
                .setContentText("This is a test notification")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setGroup("Expiry")
                .setAutoCancel(true);
                //.setContentIntent(resultPendingIntent);

        NotificationCompat.Builder summaryNotification = new NotificationCompat.Builder(context, "notifyUs")
                .setContentTitle("Things expiring")
                .setContentText("2 items")
                .setSmallIcon(R.drawable.ic_save_icon)
                .setStyle(new NotificationCompat.InboxStyle()
                            .addLine("Check this out")
                            .addLine("Hey there")
                            .setBigContentTitle("2 expiring")
                            .setSummaryText("Reminder app"))
                .setGroup("Expiry")
                .setGroupSummary(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        notificationManager.notify(0, summaryNotification.build());
    }
}