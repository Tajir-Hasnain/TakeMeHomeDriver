package com.example.tajir.takemehome_driver;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.webkit.HttpAuthHandler;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

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
            if (getReqCount() >= 15)
                pushNotification();
        }
        else if(intent.getStringExtra("status").equals("stop"))
            onDestroy();

        return START_STICKY;
    }

    public void sendDriverStatus() {
        ;
    }
    String reqCount = "0";
    public Integer getReqCount() {

        //JSON

        RequestQueue queue = Volley.newRequestQueue(this);

        Log.d("Status","Getting Request Count from server");
        String url = "http://6105b92d.ngrok.io/studentnumrequest";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    reqCount = response.getString("count");
                    Log.d("Request Count" , reqCount);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("Status","Json request failed");
            }
        });
        queue.add(jsonObjectRequest);
    //    Log.d("Request Count", reqCount);
        return Integer.valueOf(reqCount);
    }

    public void pushNotification() {
        if(isServiceRunning)    return;

        Log.d("Status","Request Count : " + getReqCount().toString() + ",Pushing Notification");
        isServiceRunning = true;

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this,"default")
                .setTimeoutAfter(900000)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.notificationText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setLights(Color.RED , 3000 , 3000)
                .setSound(alarmSound);
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
