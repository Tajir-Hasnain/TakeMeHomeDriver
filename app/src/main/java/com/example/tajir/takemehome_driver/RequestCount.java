package com.example.tajir.takemehome_driver;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

public class RequestCount extends Service {

    public RequestCount() {
    }

    public static boolean isServiceRunning = false;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public void onDestroy() {
        stopNotification();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Status","Reached RequestCount class");
        if(intent.getStringExtra("status").equals("start")) {
            sendDriverStatus();
            Log.d("Status","Background Service started");
            if (getCarCount() >= 15)
                pushNotification();
        }
        else if(intent.getStringExtra("status").equals("stop"))
            onDestroy();

        return START_STICKY;
    }

    public void sendDriverStatus() {
        ;
    }

    public Integer getCarCount() {
        return 15;
    }

    public void pushNotification() {
        if(isServiceRunning)    return;

        Log.d("Status","Request Count : " + getCarCount().toString() + ",Pushing Notification");
        isServiceRunning = true;

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        NotificationCompat.Builder notification = new NotificationCompat.Builder(this,"default")
                .setTimeoutAfter(900000)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.notificationText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1,notification.build());

    }

    public void stopNotification() {
        Log.d("Status", "Stopping Background Service");
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(1);
        stopSelf();
        isServiceRunning = false;
    }

}
