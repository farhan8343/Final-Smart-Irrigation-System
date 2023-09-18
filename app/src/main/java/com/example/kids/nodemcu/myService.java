package com.example.kids.nodemcu;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.smartirrigationsystem.kids.nodemcu.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class myService extends Service {

    SharedPreferences pref;
    DatabaseReference myRef;
    float currentLevelSensorA = 0;
    float currentLevelSensorB = 0;
    float expectedNotificationLevel = 0;
    long value = 0;
    DataSnapshot ds;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode

        float f = pref.getFloat("expected_notification_level", 0);
        if (f != 0.0)
            expectedNotificationLevel = f;
        else
            Log.w("User Expected Data", String.valueOf(f));


        myRef = FirebaseDatabase.getInstance().getReference();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                // long moter = dataSnapshot.child("MOTER").getValue(long.class);
                long sensorA = dataSnapshot.child("SENSOR_A").getValue(long.class);
                long sensorB = dataSnapshot.child("SENSOR_B").getValue(long.class);
                long motor_status = dataSnapshot.child("Motor_response").getValue(long.class);

                currentLevelSensorA = (float) sensorA;
                currentLevelSensorB = (float) sensorB;

                if (currentLevelSensorA < (int) expectedNotificationLevel || currentLevelSensorA < 30) {

                    com.example.kids.nodemcu.Function notification = new Function();
                    notification.addNotification(
                            getApplicationContext(),
                            "Water Level Indication",
                            "Water level of field one is " + (currentLevelSensorA),
                            R.drawable.ic_launcher_background
                    );

                }
                if (currentLevelSensorB < (int) expectedNotificationLevel || currentLevelSensorB < 30) {

                    com.example.kids.nodemcu.Function notification = new Function();
                    notification.addNotification(
                            getApplicationContext(),
                            "Water Level Indication",
                            "Water level of field two is " + (currentLevelSensorB),
                            R.drawable.ic_launcher_background
                    );
                }
                ds = dataSnapshot;
                if (motor_status == 1) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ds.child("Motor_response").getRef().setValue(value);
                        }
                    }, 300000); // 5 minutes
                }


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Failed to read value.", error.toException());
            }
        });


        //TODO do something useful
        return Service.START_STICKY;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        //Intent broadcastIntent = new Intent(this, SensorRestarterBroadcastReceiver.class);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, SensorRestarterBroadcastReceiver.class);
        this.sendBroadcast(broadcastIntent);
// startService(new Intent(this, myService.class));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }
}
