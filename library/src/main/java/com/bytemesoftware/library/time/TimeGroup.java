package com.bytemesoftware.library.time;

/**
 * Created by daniel on 6/27/14.
 */
public class TimeGroup {

    public static final int EMPTY    = -1;
    public static final int ACTIVE   = 1;
    public static final int INACTIVE = 0;


    protected static int[] updateValues(int[] values, int value) {
        for (int i = values.length - 1; i >= 0; i--) {
            if (values[i] != EMPTY) {
                values[i] = (value >> i) & ACTIVE;
            }
        }

        return values;
    }

}
