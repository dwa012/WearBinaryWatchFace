package com.bytemesoftware.library.time;

import android.content.Context;
import android.text.format.Time;

/**
 * Created by daniel on 6/27/14.
 */
public class Hours extends TimeGroup {
    private static int[] column1 = {
            INACTIVE,
            INACTIVE,
            EMPTY,
            EMPTY
    };

    private static int[] column2 = {
            INACTIVE,
            INACTIVE,
            INACTIVE,
            INACTIVE
    };

    public static int[][] updateValueswWithTime(Context context, Time time) {
        int[][] result = new int[2][4];

        int currentHour = time.hour;

//        // check for 14 hour time
//        if (DateFormat.is24HourFormat(context)) {
//            currentHour = time.get(Calendar.HOUR_OF_DAY);
//        } else {
//            currentHour = time.get(Calendar.HOUR);
//
//            // when on 12 hour time, 12 o'clock is 0, we need to adjust for that
//            if (currentHour == 0) {
//                currentHour = 12;
//            }
//        }

        int columnOneComponent = currentHour / 10;
        int columnTwoComponent = currentHour % 10;

        result[0] = updateValues(column1, columnOneComponent);
        result[1] = updateValues(column2, columnTwoComponent);

        return result;
    }
}
