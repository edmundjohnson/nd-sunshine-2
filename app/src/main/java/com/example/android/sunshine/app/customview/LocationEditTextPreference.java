package com.example.android.sunshine.app.customview;

import com.example.android.sunshine.app.Utility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import com.google.android.gms.location.places.ui.PlacePicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.SettingsActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;


/**
 * Custom EditTextPreference with validation added.
 * @author Edmund Johnson
 */
public class LocationEditTextPreference extends EditTextPreference {

    private static final int DEFAULT_MINIMUM_LOCATION_LENGTH = 2;

    private int mMinLength;

    // The field containing the location
    private EditText mEditText;

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

        // Check to see if Google Play services is available. The Place Picker API is available
        // through Google Play services, so if this is false, we'll just carry on as though this
        // feature does not exist. If it is true, however, we can add a widget to our preference.
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode == ConnectionResult.SUCCESS) {
            // Add the get current location widget to our location preference
            setWidgetLayoutResource(R.layout.pref_current_location);
        }
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        View currentLocation = view.findViewById(R.id.current_location);
        currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getContext();

                // Launch the PlacePicker, so that the user can specify their location,
                // and then return the result to SettingsActivity.
                PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                // Set the PlacePicker initial location to the currently-selected location.
                // If no location is selected, it will default to the device's location.
                if (Utility.isLocationLatLongAvailable(context)) {
                    LatLng latLng = new LatLng(
                            Utility.getLocationLatitude(context),
                            Utility.getLocationLongitude(context));
                    LatLngBounds latLngBounds = new LatLngBounds.Builder()
                            .include(latLng)
                            .build();
                    intentBuilder = intentBuilder.setLatLngBounds(latLngBounds);
                }

                // We are currently in a view, not an activity, so we need to get an activity
                // which we can use to start our PlacePicker intent.  By using SettingsActivity
                // in this way, we can ensure the result of the PlacePicker intent comes to the
                // right place for us to process it
                Activity settingsActivity = (SettingsActivity) context;
                try {
                    Intent intent = intentBuilder.build(settingsActivity);
                    // Start the Intent by requesting a result, identified by a request code.
                    settingsActivity.startActivityForResult(
                            intent, SettingsActivity.PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesNotAvailableException
                        | GooglePlayServicesRepairableException e) {
                    // What did you do??
                    // The difference in these exception types is the difference between pausing
                    // for a moment to prompt the user to update/install/enable Play services
                    // (GooglePlayServicesRepairableException) vs complete and utter failure.
                    // If you prefer to manage Google Play services dynamically, then you can do so
                    // by responding to these exceptions in the right moment. But I prefer a cleaner
                    // user experience, which is why you check all of this when the app resumes,
                    // and then disable/enable features based on that availability.
                }
            }
        });

        return view;
    }

    /**
     * Shows the dialog associated with this Preference. This is normally initiated
     * automatically on clicking on the preference. Call this method if you need to
     * show the dialog on some other event.
     *
     * @param state Optional instance state to restore on the dialog
     */
    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        mEditText = getEditText();

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Dialog dialog = getDialog();
                if (dialog instanceof AlertDialog) {
                    AlertDialog alertDialog = (AlertDialog) dialog;
                    Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                    if (mEditText.getText() == null
                            || mEditText.getText().length() < getMinLength()) {
                        positiveButton.setEnabled(false);
                    } else {
                        positiveButton.setEnabled(true);
                    }
                }
            }
        });
    }

    // Getters and setters

    public int getMinLength() {
        return mMinLength;
    }
//    public void setMinLength(int minLength) {
//        this.mMinLength = minLength;
////        invalidate();
////        requestLayout();
//    }

}
