package com.bytemesoftware.wear.faces.binary;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bytemesoftware.library.CommonConstants;
import com.bytemesoftware.wear.faces.binary.R;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;

public class ColorChooserActivity extends Activity {

    private ColorPicker picker;
    private WearConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_chooser);

        // initialize the color picker
        picker = (ColorPicker) findViewById(R.id.picker);
        picker.setOldCenterColor(getOldColor());
        picker.setColor(getOldColor());

        connection = new WearConnection(getBaseContext());

        findViewById(R.id.chooseColorButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOldColor(picker.getColor());
                connection.sendColor(CommonConstants.DATA_PATH_DOT_COLOR, picker.getColor());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        connection.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        connection.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.color_chooser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setOldColor(int color) {
        picker.setOldCenterColor(color);
        getSharedPreferences(Constants.PREFS, MODE_PRIVATE).edit().putInt(Constants.OLD_COLOR_PREFS_KEY, color).commit();
    }

    private int getOldColor() {
        return getSharedPreferences(Constants.PREFS, MODE_PRIVATE).getInt(Constants.OLD_COLOR_PREFS_KEY, Color.GREEN);
    }
}
