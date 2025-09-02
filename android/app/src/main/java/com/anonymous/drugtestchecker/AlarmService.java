package com.anonymous.drugtestchecker;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Build;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.util.Log;

import com.facebook.react.HeadlessJsTaskService;

public class AlarmService extends Service {
    private static final String TAG = "AlarmService";
    private static final String CHANNEL_ID = "AlarmServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Alarm Service", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
            Notification n = new Notification.Builder(this, CHANNEL_ID).setContentTitle("Drug Test Checker").setContentText("Checking...").build();
            startForeground(1, n);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting Headless JS task");
        Intent taskIntent = new Intent(getApplicationContext(), HeadlessTaskService.class);
        HeadlessJsTaskService.acquireWakeLockNow(getApplicationContext());
        getApplicationContext().startService(taskIntent);
        HeadlessJsTaskService.sendData(getApplicationContext(), new android.os.Bundle());
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
