package com.tom.myapplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import java.util.ArrayList;

public class RunningHelper {
    private static final String TAG = RunningHelper.class.getName();
    private ArrayList<RunningManager.Callback> mCallbacks = new ArrayList<>();
    private MyService mService;
    private boolean mBound = false;
    private static RunningHelper helper = new RunningHelper();
    private static final Object mLock = new Object();


    private static ResultReceiver mResultReceiver = new ResultReceiver(RunningHelper.class, new ResultReceiver.ResultReceiverCallback() {
        @Override
        public void onHandleCallback(Message msg) {
            //Update progress here
            switch (msg.what) {
                case MyService.UPDATE_PROGRESS:
                    helper.handleProgressChanged(msg.arg1);
                    break;
                case MyService.COMPLETE:
                    helper.handleOnComplete(msg.arg1);
                    break;
                case MyService.STATE_CHANGE:
                    helper.handleStateChanged(msg.arg1);
                    break;
            }
        }
    });
    private static Messenger callbackMessenger = new Messenger(mResultReceiver);

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MyService.LocalBinder binder = (MyService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public static RunningHelper getInstance() {
        return helper;
    }


    public void bind(Context context) {
        Log.d(TAG, "bind service");
        Intent intent = new Intent(context, MyService.class);
        intent.putExtra(MyService.EXTRA_CALLBACK, callbackMessenger);
        context.startForegroundService(intent);
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void startRunningDirect(Context context) {
        Log.d(TAG, "startRunningDirect");
        Intent intent = new Intent(context, MyService.class);
        intent.putExtra(MyService.EXTRA_CALLBACK, callbackMessenger);
        intent.setAction(MyService.ACTION_START_SERVICE);
        context.startForegroundService(intent);
    }


    public void unBind(Context context) {
        Log.d(TAG, "unbind service");

        context.unbindService(mConnection);
    }

    public boolean startRunning() {
        Log.d(TAG, "startRunning");
        if (mBound) {
            return mService.startRunning();
        } else {
            Log.e(TAG, "To go binding first");
        }
        return false;
    }

    public void stopRunning() {
        Log.d(TAG, "stopRunning");
        if (mBound) {
            mService.cancelRunning();
        } else {
            Log.e(TAG, "To go binding first");
        }
    }

    public synchronized boolean isRunning() {
        return RunningManager.getInstance().getState() != RunningManager.STATE_IDLE;
    }

    public void registerCallback(RunningManager.Callback callback) {
        synchronized (mLock) {
            if (callback != null && !mCallbacks.contains(callback)) {
                mCallbacks.add(callback);
            }
        }
    }

    public void unregisterCallback(RunningManager.Callback callback) {
        synchronized (mLock) {
            if (callback != null) {
                mCallbacks.remove(callback);
            }
        }

    }

    private void handleStateChanged(final int state) {
        synchronized (mLock) {
            for (final RunningManager.Callback callback : mCallbacks) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onStateChanged(state);
                    }
                }).start();
            }
        }
    }

    private void handleProgressChanged(final int progress) {
        synchronized (mLock) {
            for (final RunningManager.Callback callback : mCallbacks) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onProgressChanged(progress);
                    }
                }).start();
            }
        }
    }

    private void handleOnComplete(final int errNum) {
        synchronized (mLock) {
            for (final RunningManager.Callback callback : mCallbacks) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onComplete(errNum);
                    }
                }).start();
            }
        }
    }


}
