package com.bytemesoftware.library.time;

import android.content.Context;
import android.text.format.Time;

/**
 * Created by daniel on 6/27/14.
 */
public class Seconds extends TimeGroup {

    private static int[] column1 = {
            INACTIVE,
            INACTIVE,
            INACTIVE,
            EMPTY
    };

    private static int[] column2 = {
            INACTIVE,
            INACTIVE,
            INACTIVE,
            INACTIVE
    };

    public static int[][] updateValueswWithTime(Context context,  Time time) {
       int[][] result = new int[2][4];

       int currentSecond = time.second;

       int columnOneComponent = currentSecond / 10;
       int columnTwoComponent = currentSecond % 10;

        result[0] = updateValues(column1, columnOneComponent);
        result[1] = updateValues(column2, columnTwoComponent);

        return result;
    }
}
