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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

import static android.support.v4.app.LoaderManager.LoaderCallbacks;

/**
 * Encapsulates fetching the forecast and displaying it as a {@link ListView} layout.
 */
public class ForecastFragment extends Fragment
        implements LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();

    private static final String KEY_POSITION = "KEY_POSITION";

    private static  final int FORECAST_LOADER_ID = 1;

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;
    private static final int COL_COORD_LAT = 7;
    private static final int COL_COORD_LONG = 8;

    private ForecastAdapter mForecastAdapter;

    private ListView mListView;
    private int mSelectedPosition;
    private boolean mUseTodayLayout;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
//            case R.id.action_refresh:
//                updateWeather();
//                return true;

            case R.id.action_map:
                openPreferredLocationInMap();
                return true;

            default:
                Log.w(LOG_TAG, "Unknown menu option");
                return super.onOptionsItemSelected(item);
        }
    }

    private void openPreferredLocationInMap() {
//        String location = Utility.getPreferredLocation(getActivity());

        if (null != mForecastAdapter) {
            Cursor c = mForecastAdapter.getCursor();
            if (c != null) {
                String lat = c.getString(COL_COORD_LAT);
                String lon = c.getString(COL_COORD_LONG);

                // Using the URI scheme for showing a location found on a map.  This super-handy
                // intent is detailed in the "Common Intents" page of Android's developer site:
                // http://developer.android.com/guide/components/intents-common.html#Maps
                Uri geoLocation = Uri.parse("geo:" + lat + "," + lon);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "Couldn't display map for location (" + lat + ", " + lon + "), no receiving apps installed!");
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        String locationSetting = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());
        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        Cursor cur = getActivity().getContentResolver().query(
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);

        // The ForecastAdapter will take data from our cursor and populate the ListView.
        // We cannot use FLAG_AUTO_REQUERY since it is deprecated, so we will end
        // up with an empty list the first time we run.
        mForecastAdapter = new ForecastAdapter(getActivity(), cur, 0);
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mListView.setEmptyView(rootView.findViewById(R.id.listview_forecast_empty));
        mListView.setAdapter(mForecastAdapter);

        // Create a listener for clicking on the list item.
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    mSelectedPosition = position;
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    long date = cursor.getLong(COL_WEATHER_DATE);
                    Uri dateUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, date);
                    ((Callback) getActivity()).onItemSelected(dateUri);
                }
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_POSITION)) {
            // The listview probably hasn't even been populated yet.
            // Actually perform the swapout in onLoadFinished.
            mSelectedPosition = savedInstanceState.getInt(KEY_POSITION);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When device rotates, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to ListView.INVALID_POSITION,
        // so check for that before storing.
        if (mSelectedPosition != ListView.INVALID_POSITION) {
            outState.putInt(KEY_POSITION, mSelectedPosition);
        }
        super.onSaveInstanceState(outState);
    }

    private void updateWeather() {
        SunshineSyncAdapter.syncImmediately(getActivity());

//        //Old version, before SyncAdapter
//        String location = Utility.getPreferredLocation(getActivity());
//        Intent alarmIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
//        alarmIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, location);
//
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent,
//                PendingIntent.FLAG_ONE_SHOT);
//        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//        // Set an alarm to broadcast the intent in 5 seconds
//        alarmManager.set(AlarmManager.RTC_WAKEUP,
//                System.currentTimeMillis() + 5000,
//                pendingIntent);

//        //Old version, uses AsyncTask
//        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
//        weatherTask.execute(location);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getLoaderManager().initLoader(FORECAST_LOADER_ID, bundle, this);
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param i the ID of the loader being created.
     * @param bundle the arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String location = Utility.getPreferredLocation(getActivity());

        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, filter the query to return weather only for
        // dates after or including today.
        long startDate = System.currentTimeMillis();
        Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(location, startDate);

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        return new CursorLoader(getActivity(),
                uri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See {@link FragmentManager#beginTransaction()
     * FragmentManager.openTransaction()} for further discussion on this.
     * <p/>
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     * <p/>
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the {@link CursorAdapter#CursorAdapter(Context,
     * Cursor, int)} constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link android.content.CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param cursorLoader the Loader that has finished.
     * @param cursor the cursor generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mForecastAdapter.swapCursor(cursor);

        // If we don't need to restart the loader, and there's a desired position
        // to restore to, do so now.
        if (mSelectedPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mSelectedPosition);
            //((LinearLayout) mListView.getItemAtPosition(mSelectedPosition)).setActivated(true);
        }
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (mForecastAdapter.getCount() == 0) {
            TextView tv = (TextView) getActivity().findViewById(R.id.listview_forecast_empty);
            if (null != tv) {
                String message;
                if (!Utility.isNetworkConnection(getContext())) {
                    message = getString(R.string.empty_forecast_list_no_network);
                } else {
                    @SunshineSyncAdapter.LocationStatus int locationStatus = Utility.getLocationStatus(getContext());
                    switch (locationStatus) {
                        case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                            message = getString(R.string.empty_forecast_list_server_down);
                            break;
                        case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                            message = getString(R.string.empty_forecast_list_server_error);
                            break;
                        case SunshineSyncAdapter.LOCATION_STATUS_INVALID:
                            message = getString(R.string.empty_forecast_list_invalid_location);
                            break;
                        case SunshineSyncAdapter.LOCATION_STATUS_OK:
                        case SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN:
                        default:
                            message = getString(R.string.empty_forecast_list);
                            break;
                    }
                }
                tv.setText(message);
            }
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param cursorLoader the Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mForecastAdapter.swapCursor(null);
    }

    public void onLocationChanged(Bundle bundle) {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER_ID, bundle, this);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mForecastAdapter != null) {
            mForecastAdapter.setUseTodayLayout(useTodayLayout);
        }
    }

    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     * <p/>
     * <p>This callback will be run on your main thread.
     *
     * @param sharedPreferences The {@link SharedPreferences} that received
     *                          the change.
     * @param key               The key of the preference that was changed, added, or
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getString(R.string.pref_location_status_key).equals(key)) {
            updateEmptyView();
        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri dateUri);
    }

}
