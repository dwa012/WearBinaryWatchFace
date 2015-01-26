package com.bytemesoftware.wear.faces.binary.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bytemesoftware.wear.faces.binary.Constants;
import com.bytemesoftware.wear.faces.binary.R;
import com.bytemesoftware.library.CommonConstants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by daniel on 7/4/14.
 */
public class BinaryWatchFaceConfigListenerService extends WearableListenerService {

    private static final String TAG = "DataLayerSample";
    private SharedPreferences preferences;

    //  keys for the data map
    private String KEY_DOT_COLOR;
    private String KEY_BACKGROUND_COLOR;
    private String KEY_LAYOUT;
    private String KEY_SHOW_DIGITAL_TIME;

    GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();

        KEY_DOT_COLOR = getResources().getString(R.string.pref_key_dot_color);
        KEY_BACKGROUND_COLOR = getResources().getString(R.string.pref_key_background_color);
        KEY_LAYOUT = getResources().getString(R.string.pref_key_change_layout);
        KEY_SHOW_DIGITAL_TIME = getResources().getString(R.string.pref_key_show_digital_time);

        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        LOGD(TAG, "onDataChanged: " + dataEvents);

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();

        if(!mGoogleApiClient.isConnected()) {
            ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);

            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "BinaryWatchFaceConfigListenerService failed to connect to GoogleApiClient.");
                return;
            }
        }

        // Loop through the events and send a message back to the node that created the data item.
        for (DataEvent event : events) {
            Uri uri = event.getDataItem().getUri();
            String path = uri.getPath();

            DataMapItem item = DataMapItem.fromDataItem(event.getDataItem());

            switch (path) {
                case CommonConstants.DATA_PATH_DOT_COLOR:
                    putIntPreference(KEY_DOT_COLOR, item.getDataMap().getInt(CommonConstants.DATA_VALUE));
                    break;

                case CommonConstants.DATA_PATH_BACKGROUND_COLOR:
                    putIntPreference(KEY_BACKGROUND_COLOR, item.getDataMap().getInt(CommonConstants.DATA_VALUE));
                    break;

                case CommonConstants.DATA_PATH_DOT_COLOR_RESET:
                    removePreference(KEY_DOT_COLOR);
                    break;

                case CommonConstants.DATA_PATH_BACKGROUND_COLOR_RESET:
                    removePreference(KEY_BACKGROUND_COLOR);
                    break;

                case CommonConstants.DATA_PATH_CHANGE_LAYOUT:
                    putIntPreference(KEY_LAYOUT, item.getDataMap().getInt(CommonConstants.DATA_VALUE, CommonConstants.PREFS_LAYOUT_VERTICAL));
                    break;

                case CommonConstants.DATA_PATH_SHOW_DIGITAL_TIME:
                    putBooleanPreference(KEY_SHOW_DIGITAL_TIME, item.getDataMap().getBoolean(CommonConstants.DATA_VALUE, false));
            }

            getBaseContext().sendBroadcast(new Intent(Constants.NEW_CONFIGURATION_RECEIVED));

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

    private void removePreference(String key) {
        preferences
                .edit()
                .remove(key)
                .apply();
    }

    private void putIntPreference(String key, int value) {
        preferences
                .edit()
                .putInt(key, value)
                .apply();
    }

    private void putBooleanPreference(String key, boolean value) {
        preferences
                .edit()
                .putBoolean(key, value)
                .apply();
    }
}
