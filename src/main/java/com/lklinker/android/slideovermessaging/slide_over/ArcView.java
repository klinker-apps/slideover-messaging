/*
* Copyright 2013 Luke Klinker
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.lklinker.android.slideovermessaging.slide_over;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.lklinker.android.slideovermessaging.R;

import java.util.ArrayList;

public class ArcView extends ViewGroup {
    public static Context mContext;

    public static float TEXT_SIZE;
    public static float TEXT_GAP;

    public Bitmap halo;
    public Bitmap clear;

    public Paint newMessagePaint;
    public Paint conversationsPaint;
    public Paint closePaint;
    public Paint movePaint;
    public Paint clearPaint;
    public Paint[] textPaint;

    public float radius;
    public float breakAngle;

    public SharedPreferences sharedPrefs;

    public static int height;
    public static int width;

    public double sliverPercent;

    public static ArrayList<String[]> newConversations;

    public ArcView(Context context, Bitmap halo, float radius, float breakAngle, double sliverPercent) {
        super(context);

        mContext = context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        TEXT_SIZE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, context.getResources().getDisplayMetrics());
        TEXT_GAP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 2, context.getResources().getDisplayMetrics());

        Display d = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        height = d.getHeight();
        width = d.getWidth();

        newMessagePaint = new Paint();
        newMessagePaint.setAntiAlias(true);
        newMessagePaint.setColor(Color.WHITE);
        newMessagePaint.setAlpha(SlideOverService.START_ALPHA2);
        newMessagePaint.setStyle(Paint.Style.STROKE);
        newMessagePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics()));

        float dashLength = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());

        conversationsPaint = new Paint(newMessagePaint);
        conversationsPaint.setAlpha(SlideOverService.START_ALPHA);
        conversationsPaint.setPathEffect(new DashPathEffect(new float[]{dashLength, dashLength * 2}, 0));

        closePaint = new Paint();
        closePaint.setAntiAlias(true);
        closePaint.setColor(Color.WHITE);
        closePaint.setAlpha(SlideOverService.START_ALPHA);
        closePaint.setTextSize(TEXT_SIZE);
        closePaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

        movePaint = new Paint(closePaint);
        clearPaint = new Paint(closePaint);

        newConversations = new ArrayList<String[]>();

        textPaint = new Paint[newConversations.size()];

        for (int x = 0; x < newConversations.size(); x++) {
            textPaint[x] = new Paint(closePaint);
        }

        for (int i = 0; i < newConversations.size(); i++) {
            textPaint[i].setAlpha(SlideOverService.START_ALPHA2);
        }

        this.halo = halo;
        this.radius = radius;
        this.breakAngle = breakAngle;
        this.sliverPercent = sliverPercent;
    }


    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (newConversations.size() > 0) {
            if (SlideOverService.PERCENT_DOWN_SCREEN > height / 2) {
                canvas.drawText(getResources().getString(R.string.slideover_clear), (float) ((width * .25) - (closePaint.measureText(getResources().getString(R.string.slideover_clear)) / 2)), 60, clearPaint);
                canvas.drawText(getResources().getString(R.string.slideover_close), (float) ((width * .5) - (closePaint.measureText(getResources().getString(R.string.slideover_close)) / 2)), 60, closePaint);
                canvas.drawText(getResources().getString(R.string.slideover_settings_overlay), (float) ((width * .75) - (closePaint.measureText(getResources().getString(R.string.slideover_settings_overlay)) / 2)), 60, movePaint);
            } else {
                canvas.drawText(getResources().getString(R.string.slideover_clear), (float) ((width * .25) - (closePaint.measureText(getResources().getString(R.string.slideover_clear)) / 2)), height - 100, clearPaint);
                canvas.drawText(getResources().getString(R.string.slideover_close), (float) ((width * .5) - (closePaint.measureText(getResources().getString(R.string.slideover_close)) / 2)), height - 100, closePaint);
                canvas.drawText(getResources().getString(R.string.slideover_settings_overlay), (float) ((width * .75) - (closePaint.measureText(getResources().getString(R.string.slideover_settings_overlay)) / 2)), height - 100, movePaint);
            }
        } else {
            if (SlideOverService.PERCENT_DOWN_SCREEN > height / 2) {
                canvas.drawText(getResources().getString(R.string.slideover_close), (float) ((width * .25) - (closePaint.measureText(getResources().getString(R.string.slideover_close)) / 2)), 60, closePaint);
                canvas.drawText(getResources().getString(R.string.slideover_settings_overlay), (float) ((width * .75) - (closePaint.measureText(getResources().getString(R.string.slideover_settings_overlay)) / 2)), 60, movePaint);
            } else {
                canvas.drawText(getResources().getString(R.string.slideover_close), (float) ((width * .25) - (closePaint.measureText(getResources().getString(R.string.slideover_close)) / 2)), height - 100, closePaint);
                canvas.drawText(getResources().getString(R.string.slideover_settings_overlay), (float) ((width * .75) - (closePaint.measureText(getResources().getString(R.string.slideover_settings_overlay)) / 2)), height - 100, movePaint);
            }
        }

        int[] point = getPosition();

        // Draws the arcs that you can interact with
        if (sharedPrefs.getString("slideover_side", "left").equals("left")) {
            RectF oval = new RectF(-1 * radius, point[1] + (halo.getHeight() / 2) - radius, radius, point[1] + (halo.getHeight() / 2) + radius);

            Path newMessagePath = new Path();
            newMessagePath.addArc(oval, breakAngle, -180);

            Path conversationsPath = new Path();
            conversationsPath.addArc(oval, breakAngle, 180);

            canvas.drawPath(newMessagePath, newMessagePaint);
            canvas.drawPath(conversationsPath, conversationsPaint);
        } else {
            RectF oval = new RectF(width - radius, point[1] + (halo.getHeight() / 2) - radius, width + radius, point[1] + (halo.getHeight() / 2) + radius);

            Path newMessagePath = new Path();
            newMessagePath.addArc(oval, breakAngle - 45, -180);

            Path conversationsPath = new Path();
            conversationsPath.addArc(oval, breakAngle - 45, 180);

            canvas.drawPath(newMessagePath, newMessagePaint);
            canvas.drawPath(conversationsPath, conversationsPaint);
        }

        float conversationsRadius = radius + TEXT_SIZE + TEXT_GAP;

        int x = 0;
        // Draws the new conversations from the arraylist newConversations
        for (int i = newConversations.size() - 1; i >= 0; i--) {
            if (sharedPrefs.getString("slideover_side", "left").equals("left")) {
                RectF oval = new RectF(-1 * conversationsRadius, point[1] + (halo.getHeight() / 2) - conversationsRadius, conversationsRadius, point[1] + (halo.getHeight() / 2) + conversationsRadius);

                Path textPath = new Path();
                textPath.addArc(oval, -88, 90 + breakAngle);

                canvas.drawTextOnPath(newConversations.get(i)[0] + " - " + newConversations.get(i)[1], textPath, 0f, 0f, textPaint[x]);
            } else {
                RectF oval = new RectF(width - conversationsRadius, point[1] + (halo.getHeight() / 2) - conversationsRadius, width + conversationsRadius, point[1] + (halo.getHeight() / 2) + conversationsRadius);

                Path textPath = new Path();
                textPath.addArc(oval, -180 - breakAngle + 5, breakAngle + 90);

                canvas.drawTextOnPath(newConversations.get(i)[0] + " - " + newConversations.get(i)[1], textPath, 0f, 0f, textPaint[x]);
            }

            x++;

            conversationsRadius += TEXT_SIZE + TEXT_GAP;
        }
    }


    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public void updateTextPaint() {
        textPaint = new Paint[newConversations.size()];

        for (int x = 0; x < newConversations.size(); x++) {
            textPaint[x] = new Paint();
        }

        for (int i = 0; i < newConversations.size(); i++) {
            textPaint[i].setAntiAlias(true);
            textPaint[i].setColor(Color.WHITE);
            textPaint[i].setAlpha(SlideOverService.START_ALPHA2);
            textPaint[i].setTextSize(TEXT_SIZE);
            textPaint[i].setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        }
    }

    public int[] getPosition() {
        int[] returnArray = {0, 0};

        if (sharedPrefs.getString("slideover_side", "left").equals("left")) {
            returnArray[0] = (int) (-1 * halo.getWidth() * (1 - SlideOverService.HALO_SLIVER_RATIO));
        } else {
            returnArray[0] = (int) (width - (halo.getWidth() * SlideOverService.HALO_SLIVER_RATIO));
        }

        returnArray[1] = (int) (SlideOverService.PERCENT_DOWN_SCREEN);

        return returnArray;
    }

    public static void setDisplay() {
        Display d = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        height = d.getHeight();
        width = d.getWidth();
    }
}
