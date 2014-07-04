package com.bytemesoftware.wear.faces.binary;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.wearable.view.WatchViewStub;
import android.view.View;

import com.bytemesoftware.wear.faces.binary.time.Minutes;
import com.bytemesoftware.wear.faces.binary.time.Hours;
import com.bytemesoftware.wear.faces.binary.time.Seconds;

import java.util.Calendar;

public class WatchFaceActivity extends Activity {

    private int TICK_INTERVAL = 950;



    private Handler mHandler;

    private View rootView;

    private IntentFilter filter;
    private IntentFilter dotColorChangedIntentfilter;

    private BroadcastReceiver timeUpdateReceiver;
    private BroadcastReceiver dotColorChangedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_face);

        // get the watch face stud. This is loaded asynchronously.
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                rootView = stub.getRootView(); // set the root view of the face
                setBright(); // set the mode to bright
                startRepeatingTask(); // start the seconds animation
            }
        });

        // create te handler for the seconds animation
        mHandler = new Handler(Looper.getMainLooper());

        // create the needed receivers
        initReceivers();
    }

    private void initReceivers() {
        // create the intent filter for the time tick action
        filter = new IntentFilter(Intent.ACTION_TIME_TICK);

        // create the reciever
        timeUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                    startRepeatingTask();
                    updateCompleteTime();
                }
            }
        };

        // create the intent filter for the time tick action
        dotColorChangedIntentfilter = new IntentFilter(Constants.DOT_COLOR_RECIEVED_ACTION);

        // create the reciever
        dotColorChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Constants.DOT_COLOR_RECIEVED_ACTION.equals(intent.getAction())) {
                    startRepeatingTask();
                    updateCompleteTime();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        // attach the time receiver to listen for time ticks.
        this.registerReceiver(timeUpdateReceiver, filter);
        this.registerReceiver(dotColorChangedReceiver, dotColorChangedIntentfilter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // wWen we resume, we are going to a bright state.
        // There is state where there is not a watch face does not have a view yet.
        // The guard prevents trying to update views that do not exist.
        if (rootView != null) {
            setBright();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // When the face is paused, we are going to go into the dimmed state
        if (rootView != null) setDimmed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Traditionally this is done in the onPause.
        // But since we are going to still get time updates
        // when paused, we can unregister the receiver
        this.unregisterReceiver(timeUpdateReceiver);
        this.unregisterReceiver(dotColorChangedReceiver);
    }

    //========================================================================
    // HELPERS FOR UPDATING THE VIEW WITH THE CURRENT TIME
    //========================================================================

    private void updateCompleteTime() {
        View view = this.findViewById(android.R.id.content).getRootView();

        Calendar calendar = Calendar.getInstance();

        Minutes.updateViewWithTime(view, calendar);
        Hours.updateViewWithTime(view, calendar);

        Seconds.updateViewWithTime(view, calendar);
    }

    private void updateSeconds() {
        View view = this.findViewById(android.R.id.content).getRootView();

        Calendar calendar = Calendar.getInstance();

        Seconds.updateViewWithTime(view, calendar);
    }

    private void clearSeconds() {
        View view = this.findViewById(android.R.id.content).getRootView();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);

        Seconds.updateViewWithTime(view, calendar);
    }

    //========================================================================
    // HELPERS FOR CHANGING THE COLOR STATES BASED ON BEING DIMMED OR NOT
    //========================================================================

    private void setDimmed() {
        // set the dimmed flag
        this.getSharedPreferences(Constants.PREFS, MODE_PRIVATE).edit().putBoolean(Constants.PREFS_DIMMED_KEY, true).commit();

        // stop the second updates
        stopRepeatingTask();

        // update the time
        updateCompleteTime();

        //clear the seconds value
        clearSeconds();
    }

    private void setBright() {
        this.getSharedPreferences(Constants.PREFS, MODE_PRIVATE).edit().putBoolean(Constants.PREFS_DIMMED_KEY, true).commit();
        updateCompleteTime();

        // start the seconds animation again
        startRepeatingTask();
    }

    private boolean isDimmed() {
        return this.getSharedPreferences(Constants.PREFS, MODE_PRIVATE).getBoolean(Constants.PREFS_DIMMED_KEY, false);
    }

    //========================================================================
    // CODE FOR THE SECONDS ANIMATION
    //========================================================================

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(mStatusChecker, TICK_INTERVAL);
            updateSeconds(); //this function can change value of mInterval.

        }
    };

    private void startRepeatingTask() {
        // make sure that there is not a task running
        // before starting a new task
        mHandler.removeCallbacks(mStatusChecker);
        mStatusChecker.run();
    }

    private void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

}
