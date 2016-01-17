package com.example.android.sunshine.app.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

import com.example.android.sunshine.app.R;

/**
 * @author Edmund Johnson
 */
public class LocationEditTextPreference extends EditTextPreference {

    private static final int DEFAULT_MINIMUM_LOCATION_LENGTH = 1;

    private int mMinLength;

    /**
     * Constructor.
     * @param context the context
     * @param attrs the set of attributes
     */
    public LocationEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.LocationEditTextPreference, 0, 0);
        try {
            mMinLength = a.getInteger(R.styleable.LocationEditTextPreference_minLength,
                    DEFAULT_MINIMUM_LOCATION_LENGTH);
        } finally {
            a.recycle();
        }
    }

    // Getters and setters

    public int getMinLength() {
        return mMinLength;
    }
    public void setMinLength(int minLength) {
        this.mMinLength = minLength;
//        invalidate();
//        requestLayout();
    }

}
