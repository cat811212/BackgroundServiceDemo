package com.tom.myapplication;

import android.util.Log;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RunningManager {
    private static final String TAG = "RunningManager";
    public static final int STATE_IDLE = 1;
    public static final int STATE_RUNNING = 2;
    public static final int STATE_FINISH = 3;
    public static final int COMPLETE_CANCEL = 4;
    public static final int COMPLETE_ERROR = 5;
    private static RunningManager mRunningManager = new RunningManager();
    private static AtomicInteger mRunningStatus = new AtomicInteger(STATE_IDLE);
    private Callback mCallback;
    private AtomicBoolean mIsRunning = new AtomicBoolean(false);
    private AtomicBoolean mIsCancel = new AtomicBoolean(false);

    public static RunningManager getInstance() {
        return mRunningManager;
    }

    public synchronized int getState() {
        return mRunningStatus.get();
    }

    public synchronized boolean startRunning() {
        if (mIsRunning.get()) {
            return false;
        }
        mIsRunning.set(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                setRunningState(STATE_RUNNING);
                try {
                    for (int i = 0; i <= 100; i += 5) {
                        Log.d(TAG, "Thread is running:" + i);
                        if (mIsCancel.get()) {
                            throw new CancellationException("Cancel");
                        }
                        handleProgressChanged(i);
                        Thread.sleep(1000 * 2);
                    }
                    setRunningState(STATE_IDLE);
                    handleOnComplete(STATE_FINISH);

                } catch (CancellationException e) {
                    setRunningState(STATE_IDLE);
                    handleOnComplete(COMPLETE_CANCEL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    setRunningState(STATE_IDLE);
                    handleOnComplete(COMPLETE_ERROR);
                } finally {
                    mIsRunning.set(false);
                    mIsCancel.set(false);
                }
            }
        }).start();
        return true;
    }

    public synchronized void cancelRunning() {
        mIsCancel.set(true);
    }

    public void setRunningState(int state) {
        int previous = mRunningStatus.get();
        mRunningStatus.set(state);
        if (previous != state) {
            handleStateChanged(state);
        }
    }

    private void handleStateChanged(int state) {
        if (mCallback != null)
            mCallback.onStateChanged(state);
    }

    private void handleProgressChanged(int progress) {
        if (mCallback != null)
            mCallback.onProgressChanged(progress);
    }

    private void handleOnComplete(int errNum) {
        if (mCallback != null)
            mCallback.onComplete(errNum);
    }

    public void registerCallback(Callback callback) {
        mCallback = callback;
    }

    public void unregisterCallback() {
        mCallback = null;
    }

    public interface Callback {
        void onStateChanged(int state);

        void onProgressChanged(int progress);

        void onComplete(int errNum);
    }
}
