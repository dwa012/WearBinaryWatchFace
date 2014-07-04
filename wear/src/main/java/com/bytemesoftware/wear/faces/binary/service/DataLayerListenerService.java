package com.bytemesoftware.wear.faces.binary.service;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import com.bytemesoftware.wear.faces.binary.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by daniel on 7/4/14.
 */
public class DataLayerListenerService extends WearableListenerService {

    private static final String TAG = "DataLayerSample";
    private static final String DOT_COLOR_RECEIVED_PATH = "/dot_color";
    private static final String DOT_COLOR_RESET_RECEIVED_PATH = "/dot_reset";

    GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        LOGD(TAG, "onDataChanged: " + dataEvents);
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();
        if(!mGoogleApiClient.isConnected()) {
            ConnectionResult connectionResult = mGoogleApiClient
                    .blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "DataLayerListenerService failed to connect to GoogleApiClient.");
                return;
            }
        }

        // Loop through the events and send a message back to the node that created the data item.
        for (DataEvent event : events) {
            Uri uri = event.getDataItem().getUri();
            String path = uri.getPath();
            if (DOT_COLOR_RECEIVED_PATH.equals(path)) {
                DataMapItem item = DataMapItem.fromDataItem(event.getDataItem());
                int colorValue = item.getDataMap().getInt("value");

                getBaseContext().getSharedPreferences(Constants.PREFS, MODE_PRIVATE).edit().putInt(Constants.PREFS_DOT_COLOR_KEY, colorValue).commit();
                getBaseContext().sendBroadcast(new Intent(Constants.DOT_COLOR_RECIEVED_ACTION));

            }

            if (DOT_COLOR_RESET_RECEIVED_PATH.equals(path)) {
                getBaseContext().getSharedPreferences(Constants.PREFS, MODE_PRIVATE).edit().remove(Constants.PREFS_DOT_COLOR_KEY).commit();
                getBaseContext().sendBroadcast(new Intent(Constants.DOT_COLOR_RECIEVED_ACTION));
            }
        }
    }

    @Override
    public void onPeerConnected(Node peer) {
        LOGD(TAG, "onPeerConnected: " + peer);
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        LOGD(TAG, "onPeerDisconnected: " + peer);
    }

    public static void LOGD(final String tag, String message) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message);
        }
    }
}
