package com.tom.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MyService extends Service {
    private static final String TAG = "MyService";
    public static final int UPDATE_PROGRESS = 1;
    public static final int STATE_CHANGE = 2;
    public static final int COMPLETE = 3;
    private static final int FAIL = 4;
    public static final String ACTION_START_SERVICE = "ACTION_START_SERVICE";
    public static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";
    public static final String EXTRA_CALLBACK = "runnerCallback";
    private static final String CHANNEL_ID = "MyServiceNotify";
    private static final String CHANNEL_NAME = "MyServiceNotify";
    private static final int NOTIFY_ID = 99;
    private boolean mIsCancel = false;
    private Messenger mCallback;
    RunningManager manager;
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    private RunningManager.Callback runnerCallback = new RunningManager.Callback() {
        @Override
        public void onStateChanged(int state) {
            stateChanged(state);
        }

        @Override
        public void onProgressChanged(int progress) {
            updateProgress(progress);
        }

        @Override
        public void onComplete(int errNum) {
            complete(errNum);
        }
    };

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotifyBuilder;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        MyService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MyService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        NotificationChannel notificationChannel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW);
        mNotificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(notificationChannel);
        mNotifyBuilder = new NotificationCompat.Builder(getApplicationContext(),
                CHANNEL_ID);
        mNotifyBuilder.setSmallIcon(R.drawable.ic_system_update);
        manager = RunningManager.getInstance();
        startForeground(NOTIFY_ID, mNotifyBuilder.build());
        manager.registerCallback(runnerCallback);

    }

    private void createProgressNotification() {
        mNotifyBuilder
                .setContentTitle("Service running")
                .setProgress(100, 0, false)
                .setAutoCancel(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true);
        mNotificationManager.notify(NOTIFY_ID, mNotifyBuilder.build());

    }

    private void createTextNotification(String msg) {
        mNotifyBuilder
                .setContentTitle(msg)
                .setAutoCancel(true)
                .setOngoing(false)
                .setOnlyAlertOnce(false)
                .setProgress(0, 0, false);

        mNotificationManager.notify(NOTIFY_ID, mNotifyBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (intent != null) {
            if (intent.getExtras() != null) {

                mCallback = intent.getExtras().getParcelable(EXTRA_CALLBACK);
                if (mCallback != null) {
                    Message msg = Message.obtain();
                    msg.what = STATE_CHANGE;
                    msg.arg1 = manager.getState();
                    try {
                        mCallback.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (intent.getAction() != null)
                switch (intent.getAction()) {
                    case ACTION_START_SERVICE:
                        startRunning();
                        break;
                    case ACTION_STOP_SERVICE:
                        cancelRunning();
                        break;
                }
        }
        return START_REDELIVER_INTENT;
    }

    public boolean startRunning() {
        boolean rst = startService();
        if (rst) {
            createProgressNotification();
        }
        return rst;
    }

    public void cancelRunning() {
        stopService();
    }

    private void updateProgress(int progress) {
        mNotifyBuilder.setProgress(100, progress, false);
        mNotificationManager.notify(NOTIFY_ID, mNotifyBuilder.build());
        if (mCallback != null) {
            Message msg = Message.obtain();
            msg.what = UPDATE_PROGRESS;
            msg.arg1 = progress;
            try {
                mCallback.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void stateChanged(int state) {
        if (mCallback != null) {
            Message msg = Message.obtain();
            msg.what = STATE_CHANGE;
            msg.arg1 = state;
            try {
                mCallback.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void complete(int errNum) {
        if (mCallback != null) {
            Message msg = Message.obtain();
            msg.what = COMPLETE;
            msg.arg1 = errNum;
            try {
                mCallback.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean startService() {
        startForeground(NOTIFY_ID, mNotifyBuilder.build());
        boolean rst = manager.startRunning();
        if (!rst) {
            complete(FAIL);
        }
        Log.d(TAG, "start running:" + rst);
        return rst;
    }

    private void stopService() {
        manager.cancelRunning();
        createTextNotification("Service stop");
        stopForeground(false);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        manager.unregisterCallback();
        super.onDestroy();
    }
}
