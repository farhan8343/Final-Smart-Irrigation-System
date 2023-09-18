package com.example.kids.nodemcu;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class Function {




   public void addNotification(Context context,String title,String body, int icon) {


        NotificationManager mN =
                (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    title,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(body);
            mN.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mB = new NotificationCompat.Builder(context, "default")
                .setSmallIcon(icon) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(body);// message for notification// set alarm sound for notification

        Intent intent = new Intent(context,MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mB.setContentIntent(pi);
        mN.notify(0, mB.build());

    }





}
