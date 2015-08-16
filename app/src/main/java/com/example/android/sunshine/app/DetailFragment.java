package com.example.android.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * A fragment containing the weather for a selected date.
 * @author Edmund Johnson
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String[] DETAIL_COLUMNS = {
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
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;
    private static final int COL_HUMIDITY = 5;
    private static final int COL_WIND_SPEED = 6;
    private static final int COL_DEGREES = 7;
    private static final int COL_PRESSURE = 8;
    private static final int COL_WEATHER_CONDITION_ID = 9;

    static final String DETAIL_URI = "DETAIL_URI";

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private static final int DETAIL_LOADER = 2;

    private Uri mForecastUri;
    private String mForecastStr = "";

    private ShareActionProvider mShareActionProvider;

    private TextView dayTextView;
    private TextView dateTextView;
    private TextView highTextView;
    private TextView lowTextView;
    private TextView forecastTextView;
    private TextView humidityTextView;
    private TextView windTextView;
    private TextView pressureTextView;
    private ImageView iconImageView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    public static DetailFragment newInstance(int index) {
        DetailFragment df = new DetailFragment();

        Bundle args = new Bundle();
        args.putInt("index", index);
        df.setArguments(args);

        return df;
    }

    private int getShownIndex() {
        return getArguments().getInt("index", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle args = getArguments();
        if (args != null) {
            mForecastUri = args.getParcelable(DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        dayTextView = (TextView) rootView.findViewById(R.id.day_textview);
        dateTextView = (TextView) rootView.findViewById(R.id.date_textview);
        highTextView = (TextView) rootView.findViewById(R.id.high_textview);
        lowTextView = (TextView) rootView.findViewById(R.id.low_textview);
        forecastTextView = (TextView) rootView.findViewById(R.id.forecast_textview);
        humidityTextView = (TextView) rootView.findViewById(R.id.humidity_textview);
        windTextView = (TextView) rootView.findViewById(R.id.wind_textview);
        pressureTextView = (TextView) rootView.findViewById(R.id.pressure_textview);
        iconImageView = (ImageView) rootView.findViewById(R.id.icon_imageview);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (mShareActionProvider != null ) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null");
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getLoaderManager().initLoader(DETAIL_LOADER, bundle, this);
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");

        if (null != mForecastUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(getActivity(),
                    mForecastUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null);
        }
        return null;
    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See { @ link FragmentManager#beginTransaction()
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
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader the loader that has finished.
     * @param cursor the data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            // Create a string for the sharing intent
            mForecastStr = convertCursorRowToUXFormat(cursor);

            // Write data from the cursor to the screen views
            Context context = getActivity();
            dayTextView.setText(Utility.getDayName(context, cursor.getLong(COL_WEATHER_DATE)));
            dateTextView.setText(Utility.getFormattedMonthDay(context, cursor.getLong(COL_WEATHER_DATE)));

            boolean isMetric = Utility.isMetric(getActivity());
            double highTemperature = cursor.getDouble(COL_WEATHER_MAX_TEMP);
            highTextView.setText(Utility.formatTemperature(context, highTemperature, isMetric));
            double lowTemperature = cursor.getDouble(COL_WEATHER_MIN_TEMP);
            lowTextView.setText(Utility.formatTemperature(context, lowTemperature, isMetric));

            // weather icon
            int weatherId = cursor.getInt(COL_WEATHER_CONDITION_ID);
            int resourceId = Utility.getArtResourceForWeatherCondition(weatherId);
            iconImageView.setImageDrawable(context.getResources().getDrawable(resourceId));
            forecastTextView.setText(cursor.getString(COL_WEATHER_DESC));

            float humidity = cursor.getFloat(COL_HUMIDITY);
            humidityTextView.setText(context.getString(R.string.format_humidity, humidity));
            float windSpeed = cursor.getFloat(COL_WIND_SPEED);
            float degrees = cursor.getFloat(COL_DEGREES);
            windTextView.setText(Utility.getFormattedWind(context, windSpeed, degrees, isMetric));
            float pressure = cursor.getFloat(COL_PRESSURE);
            pressureTextView.setText(context.getString(R.string.format_pressure, pressure));
        }
        if (mShareActionProvider != null ) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(getActivity());
        String highLowStr = Utility.formatTemperature(getActivity(), high, isMetric) + "/"
                + Utility.formatTemperature(getActivity(), low, isMetric);
        return highLowStr;
    }

    /*
     * This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
     * string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        String highAndLow = formatHighLows(
                cursor.getDouble(COL_WEATHER_MAX_TEMP),
                cursor.getDouble(COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(COL_WEATHER_DATE)) +
                " - " + cursor.getString(COL_WEATHER_DESC) +
                " - " + highAndLow;
    }


    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    /**
     * Perform required processing if the location changes,
     * i.e. update the Uri and restart the loader.
     * @param newLocation the new location
     */
    public void onLocationChanged(String newLocation) {
        // replace the uri, since the location has changed
        Uri uri = mForecastUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mForecastUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

}
