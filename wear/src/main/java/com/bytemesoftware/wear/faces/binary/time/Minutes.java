package com.bytemesoftware.wear.faces.binary.time;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.util.Log;
import android.view.View;

import com.bytemesoftware.wear.faces.binary.R;

import java.util.Calendar;

/**
 * Created by daniel on 6/27/14.
 */
public class Minutes extends TimeGroup {

    private static int[] column1 = {
            R.id.minute_column_1_value_1,
            R.id.minute_column_1_value_2,
            R.id.minute_column_1_value_4
    };

    private static int[] column2 = {
            R.id.minute_column_2_value_1,
            R.id.minute_column_2_value_2,
            R.id.minute_column_2_value_4,
            R.id.minute_column_2_value_8
    };

    public static void updateViewWithTime(View view, Calendar time) {
       int currentMinute = time.get(Calendar.MINUTE);

       int columnOneComponent = currentMinute / 10;
       int columnTwoComponent = currentMinute % 10;

        updateView(column1, columnOneComponent, view);
        updateView(column2, columnTwoComponent, view);

    }
}
