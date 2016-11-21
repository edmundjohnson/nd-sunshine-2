/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.example.android.sunshine.app;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.sunshine.app.customview.LocationEditTextPreference;
import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

import java.util.Locale;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 *
 * <p>See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int PLACE_PICKER_REQUEST = 123;

    /** Log tag for this class. */
    private static final String TAG = "SettingsActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_units_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_art_pack_key)));

        LocationEditTextPreference locationEditTextPreference = (LocationEditTextPreference)
                findPreference(getString(R.string.pref_location_key));
        Log.d(TAG, "onCreate: Min length = " + locationEditTextPreference.getMinLength());
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.unregisterOnSharedPreferenceChangeListener(this);

        super.onPause();
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        setPreferenceSummary(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }

        } else if (preference.getKey().equals(getString(R.string.pref_location_key))) {
            preference.setSummary(getLocationStatus(stringValue));

        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        setPreferenceSummary(preference, value);
        return true;
    }

    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     * This callback will be run on your main thread.
     *
     * @param sharedPreferences The {@link SharedPreferences} that received
     *                          the change.
     * @param key               The key of the preference that was changed, added, or
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // If the location has changed, get data for the new location
        if (key.equals(getString(R.string.pref_location_key))) {
            // Location entered by user.
            // Remove any location latitude and longitude the user created using the PlacePicker.
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(getString(R.string.pref_location_latitude_key));
            editor.remove(getString(R.string.pref_location_longitude_key));
            editor.commit();

            Utility.resetLocationStatus(this);
            SunshineSyncAdapter.syncImmediately(this);

        // If the units or icons have changed, update the lists of weather entries accordingly
        } else if (key.equals(getString(R.string.pref_units_key))
                || key.equals(getString(R.string.pref_art_pack_key))) {
            getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
        }
    }

    /**
     * Called on returning from a called activity.
     * @param requestCode the request code as reported by the activity returned from
     * @param resultCode the result returned by the activity
     * @param data the data returned by the activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check to see if the result is from our place picker
        if (requestCode == PLACE_PICKER_REQUEST) {
            // Make sure the result was successful
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                String address = place.getAddress().toString();
                LatLng latLong = place.getLatLng();

                // If the provided place doesn't have an address, we'll form a display-friendly
                // string from the latitude and longitude.
                if (TextUtils.isEmpty(address)) {
                    address = String.format(Locale.getDefault(),
                            "(%.2f, %.2f)", latLong.latitude, latLong.longitude);
                }
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.pref_location_key), address);
                // Also store the latitude and longitude so that we can use these to get a precise
                // result from our weather service. We cannot expect the weather service to
                // understand addresses that Google formats.
                if (latLong != null) {
                    editor.putFloat(getString(R.string.pref_location_latitude_key),
                            (float) latLong.latitude);
                    editor.putFloat(getString(R.string.pref_location_longitude_key),
                            (float) latLong.longitude);
                }
                // ensure that the changes are written before the SyncAdapter is invoked
                editor.commit();

                // Tell the SyncAdapter that we've changed the location, so that we can update
                // our UI with new values.  We need to do this manually because we are responding
                // to the PlacePicker widget result here, rather than allowing the
                // LocationEditTextPreference to handle these changes and invoke our callbacks.
                Preference locationPreference = findPreference(getString(R.string.pref_location_key));
                setPreferenceSummary(locationPreference, address);
                Utility.resetLocationStatus(this);
                SunshineSyncAdapter.syncImmediately(this);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }



    private String getLocationStatus(String location) {
        @SunshineSyncAdapter.LocationStatus int locationStatus = Utility.getLocationStatus(this);
        switch (locationStatus) {
            case SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN:
                return getString(R.string.pref_location_status_unknown_location, location);
            case SunshineSyncAdapter.LOCATION_STATUS_INVALID:
                return getString(R.string.pref_location_status_invalid_location, location);
            case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
            case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                return getString(R.string.pref_location_status_unable_to_validate_location, location);
            case SunshineSyncAdapter.LOCATION_STATUS_OK:
            default:
                return location;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        if (super.getParentActivityIntent() == null) {
            return null;
        }
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

}
