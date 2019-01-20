package com.tom.myapplication;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

public class ResultReceiver extends Handler {
    private WeakReference<Class> classWeakReference;
    private ResultReceiverCallback mCallback;

    ResultReceiver(Class activity, ResultReceiverCallback callback) {
        classWeakReference = new WeakReference<>(activity);
        mCallback = callback;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Class aClass = classWeakReference.get();
        if (aClass != null && mCallback != null) {
            mCallback.onHandleCallback(msg);
        }
    }

    public interface ResultReceiverCallback {
        void onHandleCallback(Message msg);
    }

}
