package com.example.android.sunshine.app.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * A custom view which is not used.
 * @author Edmund Johnson
 */
public class MyCustomView extends View {

    // NOTE: See also Lesson 5: Custom View accessibility

    // Best to declare drawing objects at the class level, so they get created and destroyed
    // less often
    private Paint paint;

    public MyCustomView(Context context) {
        super(context);
    }

    public MyCustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyCustomView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
    }

    @Override
    protected void onMeasure(int wMeasureSpec, int hMeasureSpec) {
        // Height
        int hSpecMode = MeasureSpec.getMode(hMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(hMeasureSpec);
        int myHeight;

        if (hSpecMode == MeasureSpec.EXACTLY) {
            myHeight = hSpecSize;
        } else if (hSpecMode == MeasureSpec.AT_MOST) {
            myHeight = 50;
        } else {
            myHeight = hSpecSize;
        }

        // Width
        int wSpecMode = MeasureSpec.getMode(wMeasureSpec);
        int wSpecSize = MeasureSpec.getSize(wMeasureSpec);
        int myWidth;

        if (wSpecMode == MeasureSpec.EXACTLY) {
            myWidth = wSpecSize;
        } else if (hSpecMode == MeasureSpec.AT_MOST) {
            myWidth = 50;
        } else {
            myWidth = wSpecSize;
        }

        setMeasuredDimension(myWidth, myHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint thePaint = getPaint();
        // This commented off line results in a square being drawn (fills the whole view?)
        //canvas.drawPaint(thePaint);
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, getWidth() / 2f, thePaint);
    }

    private Paint getPaint() {
        if (paint == null) {
            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
        }
        return paint;
    }

}
