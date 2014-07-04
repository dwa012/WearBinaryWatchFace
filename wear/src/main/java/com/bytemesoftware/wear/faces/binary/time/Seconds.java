package com.bytemesoftware.wear.faces.binary.time;

import android.view.View;

import com.bytemesoftware.wear.faces.binary.R;

import java.util.Calendar;

/**
 * Created by daniel on 6/27/14.
 */
public class Seconds extends TimeGroup {

    private static int[] column1 = {
            R.id.second_column_1_value_1,
            R.id.second_column_1_value_2,
            R.id.second_column_1_value_4
    };

    private static int[] column2 = {
            R.id.second_column_2_value_1,
            R.id.second_column_2_value_2,
            R.id.second_column_2_value_4,
            R.id.second_column_2_value_8
    };

    public static void updateViewWithTime(View view, Calendar time) {
       int currentSecond = time.get(Calendar.SECOND);

       int columnOneComponent = currentSecond / 10;
       int columnTwoComponent = currentSecond % 10;

        updateView(column1, columnOneComponent, view);
        updateView(column2, columnTwoComponent, view);

    }
}
