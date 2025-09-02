package com.anonymous.drugtestchecker;

import android.content.Intent;
import android.util.Log;
import com.facebook.react.HeadlessJsTaskService;

public class HeadlessTaskService extends HeadlessJsTaskService {
    private static final String TAG = "HeadlessTaskService";

    @Override
    public void onHeadlessJsTaskStart(int taskId) {
        Log.d(TAG, "Headless JS task started: " + taskId);
    }

    @Override
    public void onHeadlessJsTaskFinish(int taskId) {
        Log.d(TAG, "Headless JS task finished: " + taskId);
    }

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        // Start the named JS task
        startTask();
    }

    private void startTask() {
        // React Native will look for a task registered with the name 'DrugTestHeadless'
        HeadlessJsTaskService.acquireWakeLockNow(getApplicationContext());
        HeadlessJsTaskService.sendData(getApplicationContext(), new android.os.Bundle());
    }
}
