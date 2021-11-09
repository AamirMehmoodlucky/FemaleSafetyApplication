package com.female.safetyapp.femalesafetyapplication;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static com.female.safetyapp.femalesafetyapplication.App.CHANNEL_ID;

public class FemaleSafetyAppService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        MainActivity.MyBroadcastReceiver GPSBroadcast = new MainActivity.MyBroadcastReceiver(this);
        registerReceiver(GPSBroadcast, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

        String notificationTitle;
        int notificationColor;
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            notificationTitle = "Safety App Running";
            notificationColor = 0xff00574B;
        } else {
            notificationTitle = "GPS Turned Off";
            notificationColor = 0xffFF5722;
        }

        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(mainActivityIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification safetyServiceNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(notificationTitle)
                .setContentText("Female Safety Application")
                .setSmallIcon(R.drawable.helping_hand_240px)
                .setContentIntent(pendingIntent)
                .setColor(notificationColor)
                .build();
        startForeground(App.NOTIFICATION_ID, safetyServiceNotification);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}