package com.bytemesoftware.wear.faces.binary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.bytemesoftware.library.time.Hours;
import com.bytemesoftware.library.time.Minutes;
import com.bytemesoftware.library.time.Seconds;
import com.bytemesoftware.library.time.TimeGroup;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by danielward on 12/23/14.
 */
public class BinaryWatchFaceService extends CanvasWatchFaceService {
    private static final String TAG = "BinaryWatchFaceService";

    private static final Typeface NORMAL_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    /**
     * Update rate in milliseconds for normal (not ambient and not mute) mode. We update twice
     * a second to blink the colons.
     */
    private static final long NORMAL_UPDATE_RATE_MS = 500;

    /**
     * Update rate in milliseconds for mute mode. We update every minute, like in ambient mode.
     */
    private static final long MUTE_UPDATE_RATE_MS = TimeUnit.MINUTES.toMillis(1);


    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        static final String COLON_STRING = ":";

        /** Alpha value for drawing time when in mute mode. */
        static final int MUTE_ALPHA = 100;

        /** Alpha value for drawing time when not in mute mode. */
        static final int NORMAL_ALPHA = 255;

        private boolean preferenceChangedReceiverChanged = false;

        private BroadcastReceiver preferenceChangedReceiver;

        // dot configuration info
        int numberOfDotRows = 4;
        int numberOfDotColumns = 6;

        // dot positioning info
        float dotWidth;
        float dotMargin;

        // offsets
        float dotXOffest;
        float dotYOffest;
        float dotYExtraOffset;

        float clockYOffset;
        float mXOffset;
        float mYOffset;

        // the dot states
        int[][] hoursColumns;
        int[][] minutesColumns;
        int[][] secondsColumns;

        // the variable for the formatted time string
        String timeString = "";

        // paint variables
        Paint mBackgroundPaint;
        Paint textPaint;
        Paint interactiveActiveDotPaint;
        Paint ambientActiveDotPaint;
        Paint inactiveDotColor;

        int mInteractiveBackgroundColor = Color.BLACK;
        int mInteractiveTextColor = Color.WHITE;
        int mAmbientTextColor = Color.GRAY;


        private void init() {
            setWatchFaceStyle(new WatchFaceStyle.Builder(BinaryWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setStatusBarGravity(Gravity.LEFT | Gravity.TOP)
                    .setHotwordIndicatorGravity(Gravity.RIGHT | Gravity.TOP)
                    .setShowSystemUiTime(false)
                    .build());

            Resources resources = BinaryWatchFaceService.this.getResources();
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BinaryWatchFaceService.this);

            mYOffset = resources.getDimension(R.dimen.digital_y_offset);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(mInteractiveBackgroundColor);

            interactiveActiveDotPaint = new Paint();
            interactiveActiveDotPaint.setColor(preferences.getInt(getResources().getString(R.string.pref_key_dot_color), getResources().getColor(R.color.circle_default)));
            interactiveActiveDotPaint.setAntiAlias(true);

            ambientActiveDotPaint = new Paint();
            ambientActiveDotPaint.setColor(getResources().getColor(R.color.circle_dimmed_active));
            ambientActiveDotPaint.setAntiAlias(true);

            inactiveDotColor = new Paint();
            inactiveDotColor.setColor(getResources().getColor(R.color.circle_inactive));
            inactiveDotColor.setAntiAlias(true);

            textPaint = createTextPaint(mInteractiveTextColor, NORMAL_TYPEFACE);

            preferenceChangedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (Constants.NEW_CONFIGURATION_RECEIVED.equals(intent.getAction())) {
                        interactiveActiveDotPaint.setColor(preferences.getInt(getResources().getString(R.string.pref_key_dot_color), getResources().getColor(R.color.circle_default)));
                    }
                }
            };
        }

        private void doBackgroundComputation() {
           timeString = mTime.format("%l:%M %P").toUpperCase();

            hoursColumns = Hours.updateValueswWithTime(getBaseContext(), mTime);
            minutesColumns = Minutes.updateValueswWithTime(getBaseContext(), mTime);
            secondsColumns = Seconds.updateValueswWithTime(getBaseContext(), mTime);
        }

        private void drawTimeGroup(int[][] columnGroups, float x, float y, Canvas canvas) {
            drawTimeGroup(columnGroups, x, y , canvas, true);
        }

        private void drawTimeGroup(int[][] columnGroups, float x, float y, Canvas canvas, boolean shouldPaint) {
            // draw the hour dots
            for (int[] hoursColumn : columnGroups) {
                x += (dotWidth + dotMargin);

                float yTemp = y;

                for (int j = hoursColumn.length - 1; j >= 0; j--) {

                    if (hoursColumn[j] != TimeGroup.EMPTY) {
                        Paint paint = interactiveActiveDotPaint;

                        if(isInAmbientMode()) {
                            paint = ambientActiveDotPaint;
                        }

                        paint = (hoursColumn[j] == TimeGroup.ACTIVE && shouldPaint) ? paint : inactiveDotColor;
                        canvas.drawCircle(x + (dotWidth / 2.0f), yTemp + (dotWidth / 2.0f), (dotWidth / 2.0f), paint);

                    }

                    yTemp += (dotWidth + dotMargin);
                }
            }
        }

        private void onBurnInProtectionChanged() {
            isBurnInProtection();
        }

        private void onLowBitAmbiantChanged() {
            isLowBitAmbient();
        }

        private void onAmbientChanged() {
            if (isInAmbientMode() || mMute) {
                textPaint.setColor(mAmbientTextColor);
            } else {
                textPaint.setColor(mInteractiveTextColor);
            }
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // calculate the dot x offset once.
            if (dotXOffest == -1) {
                float mid = bounds.width() / 2.0f;
                float widthDots = (numberOfDotColumns * dotWidth) + ((numberOfDotColumns - 1) * dotMargin);
                dotXOffest = mid - (widthDots / 2.0f) - (dotWidth + dotMargin);
            }

            // calculate the dot y offset once.
            if (dotYOffest == -1) {
                float mid = bounds.height()/2.0f;
                float widthDots = (numberOfDotRows * dotWidth) + ((numberOfDotRows - 1) * dotMargin);
                dotYOffest = mid - (widthDots/2.0f) + dotYExtraOffset;
            }

            // Draw the background.
            canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);

            //--------------------------------------------------------------------------------------
            // DRAW THE DIGITAL CLOCK

            // Draw the hours.
            float y = clockYOffset;
            float x = bounds.centerX() - (textPaint.measureText(timeString))/2;

            canvas.drawText(timeString, x, y, textPaint);

            //--------------------------------------------------------------------------------------
            // DRAW THE DOTS

            y = dotYOffest;

            // draw the hours dots
            x = dotXOffest;
            drawTimeGroup(hoursColumns, x, y, canvas);

            // draw the minute dots
            x += (dotWidth + dotMargin) * hoursColumns.length;
            drawTimeGroup(minutesColumns, x, y, canvas);

            // draw the second dots
            x += (dotWidth + dotMargin) * minutesColumns.length;
            drawTimeGroup(secondsColumns, x, y, canvas, !isInAmbientMode());
        }

        // =========================================================================================
        // BOILER PLATE CODE
        // =========================================================================================

        static final int MSG_UPDATE_TIME = 0;

        // receiver state variables
        boolean mRegisteredTimeZoneReceiver = false;

        boolean mMute;

        Time mTime;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;
        boolean mBurnInProtection;

        /** How often {@link #mUpdateTimeHandler} ticks in milliseconds. */
        long mInteractiveUpdateRateMs = NORMAL_UPDATE_RATE_MS;

        /** Handler to update the time periodically in interactive mode. */
        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        if (Log.isLoggable(TAG, Log.VERBOSE)) {
                            Log.v(TAG, "updating time");
                        }

                        reDraw();

                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = mInteractiveUpdateRateMs - (timeMs % mInteractiveUpdateRateMs);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }

                        break;
                }
            }
        };

        // a receiver to listen for changes in Time Zone.
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };

        @Override
        public void onCreate(SurfaceHolder holder) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onCreate");
            }
            super.onCreate(holder);

            mTime = new Time();

            dotXOffest = -1;
            dotYOffest = -1;

            doBackgroundComputation();

            init();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }


        @Override
        public void onVisibilityChanged(boolean visible) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onVisibilityChanged: " + visible);
            }

            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceivers();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = BinaryWatchFaceService.this.getResources();

            if(!insets.isRound()) {
                dotYExtraOffset = getResources().getDimension(R.dimen.digital_y_square_extra_offset);
            }

            dotWidth  = getResources().getDimension(R.dimen.circle_width);
            dotMargin = getResources().getDimension(R.dimen.circle_margin);
            clockYOffset = getResources().getDimension(R.dimen.digital_y_offset);

            textPaint.setTextSize(resources.getDimension(R.dimen.text_size));
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);

            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);

            onLowBitAmbiantChanged();
            onBurnInProtectionChanged();

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onPropertiesChanged: burn-in protection = " + mBackgroundPaint + ", low-bit ambient = " + mLowBitAmbient);
            }
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onTimeTick: ambient = " + isInAmbientMode());
            }

            reDraw();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onAmbientModeChanged: " + inAmbientMode);
            }

            if (isLowBitAmbient()) {
                boolean antiAlias = !inAmbientMode;

                textPaint.setAntiAlias(antiAlias);
                ambientActiveDotPaint.setAntiAlias(antiAlias);
                interactiveActiveDotPaint.setAntiAlias(antiAlias);
                inactiveDotColor.setAntiAlias(antiAlias);
            }

            onAmbientChanged();

            reDraw();

            // Whether the timer should be running depends on whether we're in ambient mode (as well
            // as whether we're visible), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onInterruptionFilterChanged: " + interruptionFilter);
            }
            super.onInterruptionFilterChanged(interruptionFilter);

            boolean inMuteMode = interruptionFilter == android.support.wearable.watchface.WatchFaceService.INTERRUPTION_FILTER_NONE;

            // We only need to update once a minute in mute mode.
            setInteractiveUpdateRateMs(inMuteMode ? MUTE_UPDATE_RATE_MS : NORMAL_UPDATE_RATE_MS);

            if (mMute != inMuteMode) {
                mMute = inMuteMode;
                int alpha = inMuteMode ? MUTE_ALPHA : NORMAL_ALPHA;
                textPaint.setAlpha(alpha);

                reDraw();
            }
        }

        public void setInteractiveUpdateRateMs(long updateRateMs) {
            if (updateRateMs == mInteractiveUpdateRateMs) {
                return;
            }
            mInteractiveUpdateRateMs = updateRateMs;

            // Stop and restart the timer so the new update rate takes effect immediately.
            if (shouldTimerBeRunning()) {
                updateTimer();
            }
        }

        // -----------------------------------------------------------------------------------------
        // BinaryWatchFaceService Helpers

        private Paint createTextPaint(int defaultInteractiveColor, Typeface typeface) {
            Paint paint = new Paint();
            paint.setColor(defaultInteractiveColor);
            paint.setTypeface(typeface);
            paint.setAntiAlias(true);
            return paint;
        }

        private void registerReceivers() {
            if (!mRegisteredTimeZoneReceiver) {
                mRegisteredTimeZoneReceiver = true;
                IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
                BinaryWatchFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
            }

            if (!preferenceChangedReceiverChanged) {
                preferenceChangedReceiverChanged = true;
                IntentFilter filter = new IntentFilter(Constants.NEW_CONFIGURATION_RECEIVED);
                BinaryWatchFaceService.this.registerReceiver(preferenceChangedReceiver, filter);
            }

        }

        private void unregisterReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                mRegisteredTimeZoneReceiver = false;
                BinaryWatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
            }

            if (preferenceChangedReceiverChanged) {
                preferenceChangedReceiverChanged = false;
                BinaryWatchFaceService.this.unregisterReceiver(preferenceChangedReceiver);
            }

        }

        private void reDraw() {
            mTime.setToNow();
            doBackgroundComputation();
            invalidate();
        }
        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "updateTimer");
            }

            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);

            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        private boolean isLowBitAmbient() {
            return  mLowBitAmbient;
        }

       private boolean isBurnInProtection() {
           return mBurnInProtection;
       }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }
    }
}