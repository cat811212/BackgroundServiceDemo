package com.tom.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver {
    private static final String TAG = "Receiver";
    private static final String ACTION_START_RUNNING = "com.tom.myrunning.start.running";
    private static RunningHelper runningHelper;
    private RunningManager.Callback callback = new RunningManager.Callback() {
        @Override
        public void onStateChanged(int state) {
            Log.d(TAG, "onStateChanged:" + state);
        }

        @Override
        public void onProgressChanged(int progress) {
            Log.d(TAG, "onProgressChanged:" + progress);

        }

        @Override
        public void onComplete(int errNum) {
            Log.d(TAG, "onComplete:" + errNum);
            runningHelper.unregisterCallback(callback);
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_START_RUNNING:
                    runningHelper = RunningHelper.getInstance();
                    if (runningHelper.isRunning()) {
                        Log.e(TAG, "Is running!!");
                        return;
                    }
                    runningHelper.registerCallback(callback);
                    runningHelper.startRunningDirect(context);
                    Log.d(TAG, "start running");

                    break;
            }
        }
    }
}
