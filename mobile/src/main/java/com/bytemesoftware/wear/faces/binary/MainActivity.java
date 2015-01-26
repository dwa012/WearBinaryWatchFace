package com.bytemesoftware.wear.faces.binary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bytemesoftware.library.CommonConstants;


public class MainActivity extends Activity {

    private WearConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.changeDotButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), ColorChooserActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.resetDotButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connection.sendColor(CommonConstants.DATA_PATH_DOT_COLOR_RESET, 0);
            }
        });

        connection = new WearConnection(getBaseContext());
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
        getMenuInflater().inflate(R.menu.main, menu);
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
}
