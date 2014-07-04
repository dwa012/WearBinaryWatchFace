package com.bytemesoftware.wear.faces.binary.time;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;

import com.bytemesoftware.wear.faces.binary.R;

import java.util.Calendar;

/**
 * Created by daniel on 6/27/14.
 */
public class Hours extends TimeGroup {
    private static int[] column1 = {
            R.id.hour_column_1_value_1,
            R.id.hour_column_1_value_2
    };

    private static int[] column2 = {
            R.id.hour_column_2_value_1,
            R.id.hour_column_2_value_2,
            R.id.hour_column_2_value_4,
            R.id.hour_column_2_value_8
    };

    public static void updateViewWithTime(View view, Calendar time) {
        int currentMinute;

        if (DateFormat.is24HourFormat(view.getContext())) {
            currentMinute = time.get(Calendar.HOUR_OF_DAY);
        } else {
            currentMinute = time.get(Calendar.HOUR);
        }


        int columnOneComponent = currentMinute / 10;
        int columnTwoComponent = currentMinute % 10;

        updateView(column1, columnOneComponent, view);
        updateView(column2, columnTwoComponent, view);

    }
}
