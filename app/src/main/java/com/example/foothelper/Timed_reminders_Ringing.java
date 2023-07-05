package com.example.foothelper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Timed_reminders_Ringing extends BroadcastReceiver {
    private final static int NOTIFICATION_ID = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent notifyIntent=new Intent(context,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,notifyIntent,0);
        broadcastNotify(context, pendingIntent);
    }

    private void broadcastNotify(Context context, PendingIntent pendingIntent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new Notification.Builder(context)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("訊息")
                .setContentText("要復健囉!")
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 100, 200, 300, 400, 500})
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
