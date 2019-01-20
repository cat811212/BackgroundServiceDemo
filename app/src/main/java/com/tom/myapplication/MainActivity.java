package com.tom.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getName();
    private Button mStartServiceBT;
    private Button mStopServiceBT;
    private TextView mPercentTV;
    private ProgressBar mProgressBar;
    private RunningHelper mRunningHelper;
    private TextView mStateTV;
    private Button mStartActivityBT;
    private RunningManager.Callback mRunningCallback = new RunningManager.Callback() {
        @Override
        public void onStateChanged(final int state) {
            Log.d(TAG, "onStateChanged" + state);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mStateTV.setText("state:" + state);
                }
            });
        }

        @Override
        public void onProgressChanged(final int progress) {
            Log.d(TAG, "onProgressChanged" + progress);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setProgress(progress);
                    mPercentTV.setText(progress + "%");
                }
            });
        }

        @Override
        public void onComplete(final int errNum) {
            Log.d(TAG, "onComplete" + errNum);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mStateTV.setText("state:" + errNum);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStartServiceBT = findViewById(R.id.startServiceBT);
        mStopServiceBT = findViewById(R.id.stopServiceBT);
        mPercentTV = findViewById(R.id.precentTV);
        mProgressBar = findViewById(R.id.progressBar);
        mStateTV = findViewById(R.id.stateTV);
        mStartActivityBT = findViewById(R.id.startActivity);
        mStartActivityBT.setOnClickListener(this);
        mStartServiceBT.setOnClickListener(this);
        mStopServiceBT.setOnClickListener(this);
        mRunningHelper = RunningHelper.getInstance();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startActivity:
                break;
            case R.id.startServiceBT:
                startService();
                break;
            case R.id.stopServiceBT:
                stopService();
                break;
        }
    }

    private void startService() {
        mRunningHelper.startRunning();
    }

    private void stopService() {
        mRunningHelper.stopRunning();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRunningHelper.registerCallback(mRunningCallback);
        mRunningHelper.bind(this);

    }

    @Override
    protected void onPause() {
        mRunningHelper.unregisterCallback(mRunningCallback);
        mRunningHelper.unBind(this);
        super.onPause();

    }
}
