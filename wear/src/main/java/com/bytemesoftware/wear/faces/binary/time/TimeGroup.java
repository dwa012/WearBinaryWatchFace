package com.bytemesoftware.wear.faces.binary.time;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import com.bytemesoftware.wear.faces.binary.Constants;
import com.bytemesoftware.wear.faces.binary.R;
import com.bytemesoftware.wear.faces.binary.WatchFaceActivity;

/**
 * Created by daniel on 6/27/14.
 */
public class TimeGroup {

    protected static void updateView(int[] viewIds, int value, View view) {
        for (int i = 0; i < viewIds.length; i++) {
            int state = (value >> i) & 1;

            colorCircleView(view.findViewById(viewIds[i]), state);
        }
    }

    private static void colorCircleView(View view, int state) {
        GradientDrawable background = (GradientDrawable)view.getBackground();
        Context context = view.getContext();

        boolean isDimmed = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE).getBoolean(Constants.PREFS_DIMMED_KEY, false);

        boolean isOn = (state == 1);

        int color;

        if (isDimmed) {
            color = getBrightColor(context, isOn);
        } else {
            color = getDimmedColor(context, isOn);
        }

        background.setColor(color);

    }

    private static int getBrightColor(Context context, boolean isOn) {
        int defaultColor = context.getResources().getColor(R.color.circle_active);
        int result = context.getResources().getColor(R.color.circle_inactive);

        if (isOn) {
            result = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE).getInt(Constants.PREFS_DOT_COLOR_KEY, defaultColor);
        }

        return result;
    }

    private static int getDimmedColor(Context context, boolean isOn) {
        int defaultColor = context.getResources().getColor(R.color.circle_dimmed_active);
        int result = context.getResources().getColor(R.color.circle_dimmed_inactive);

        if (isOn) {
//            result = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE).getInt(Constants.PREFS_DOT_COLOR_KEY, defaultColor);
            result = defaultColor;
        }

        return result;
    }
}
