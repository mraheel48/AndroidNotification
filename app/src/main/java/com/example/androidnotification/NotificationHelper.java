package com.example.androidnotification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    private static final String Channel_id = "ElgoByte";
    private static final String Channel_name = "ElgoByte";
    private static final String Channel_desc = "ElgoByte Notification";
    private static final String GROUP_KEY_WORK_EMAIL = "com.example.androidnotification";

    public static void displayNotification(Context context, String title, String body) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(Channel_id, Channel_name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(Channel_desc);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

        }

        Intent intent = new Intent(context, Home.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                100,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        //Define sound URI
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, Channel_id)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setSound(soundUri)
                        .setGroup(GROUP_KEY_WORK_EMAIL)
                        .setContentIntent(pendingIntent)
                        //.setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);


        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(1, mBuilder.build());

    }

}
