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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
//    private static final int COL_WEATHER_ID = 0;
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

//    private ShareActionProvider mShareActionProvider;

//    private TextView mFriendlyDateView;
    private TextView mDateView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mDescriptionView;
    private TextView mHumidityView;
    private TextView mHumidityLabelView;
    private TextView mWindView;
    private TextView mWindLabelView;
    private TextView mPressureView;
    private TextView mPressureLabelView;
    private ImageView mIconView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

//    /**
//     * Returns a new instance of this class.
//     * @param index the index for the new instance
//     * @return a new instance of this class
//     */
//    public static DetailFragment newInstance(int index) {
//        DetailFragment df = new DetailFragment();
//
//        Bundle args = new Bundle();
//        args.putInt("index", index);
//        df.setArguments(args);
//
//        return df;
//    }

//    private int getShownIndex() {
//        return getArguments().getInt("index", 0);
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle args = getArguments();
        if (args != null) {
            mForecastUri = args.getParcelable(DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail_start, container, false);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
//        mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        mHumidityLabelView = (TextView) rootView.findViewById(R.id.detail_humidity_label_textview);
        mWindView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        mWindLabelView = (TextView) rootView.findViewById(R.id.detail_wind_label_textview);
        mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
        mPressureLabelView = (TextView) rootView.findViewById(R.id.detail_pressure_label_textview);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getActivity() instanceof DetailActivity) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailfragment, menu);
            finishCreatingMenu(menu);
        }
    }

    private void finishCreatingMenu(Menu menu) {
        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareForecastIntent());
    }

//        // Get the provider and hold onto it to set/change the share intent.
//        mShareActionProvider =
//                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
//
//        // Attach an intent to this ShareActionProvider.  You can update this at any time,
//        // like when the user selects a new piece of data they might like to share.
//        if (mShareActionProvider != null ) {
//            mShareActionProvider.setShareIntent(createShareForecastIntent());
//        } else {
//            Log.d(LOG_TAG, "Share Action Provider is null");
//        }


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
        // In the case where the master and the detail are on screen at the same time, i.e.
        // on tablets / landscape, this prevents an empty detail pane being displayed prior
        // to any list item being selected.
        // The detail card is made visible in onLoadFinished().
        if (getView() != null) {
            ViewParent vp = getView().getParent();
            if (vp instanceof CardView) {
                ((View)vp).setVisibility(View.INVISIBLE);
            }
        }
        return null;
    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See { @ link FragmentManager#beginTransaction()
     * FragmentManager.openTransaction()} for further discussion on this.
     * </p>
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     * </p>
     *
     * <ul>
     * <li>
     * The Loader will monitor for changes to the data, and report
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
     * here.</li>
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </li>
     * </ul>
     *
     * @param loader the loader that has finished.
     * @param cursor the data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            if (getView() != null) {
                ViewParent vp = getView().getParent();
                if (vp instanceof CardView) {
                    ((View)vp).setVisibility(View.VISIBLE);
                }
            }

            // Write data from the cursor to the screen views

            // weather description
            // TO DO: Use the weather id to get the description, so it can be internationalised
            String description = cursor.getString(COL_WEATHER_DESC);
            mDescriptionView.setText(description);
            // for accessibility, add a content description
            mDescriptionView.setContentDescription(getString(R.string.a11y_forecast, description));

            Context context = getActivity();
            // weather condition id
            int weatherId = cursor.getInt(COL_WEATHER_CONDITION_ID);

            // weather icon
//            int resourceId = Utility.getArtResourceForWeatherCondition(weatherId);
//            //mIconView.setImageDrawable(context.getResources().getDrawable(resourceId));
//            mIconView.setImageResource(resourceId);

            if ( Utility.usingLocalGraphics(getActivity()) ) {
                mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
            } else {
                // Glide library call.
                // .error(...) indicates a resource to be used if the load resource cannot be loaded
                // .crossFade() indicates a transition animation (?)
                Glide.with(this)
                        .load(Utility.getArtUrlForWeatherCondition(getActivity(), weatherId))
                        .error(Utility.getArtResourceForWeatherCondition(weatherId))
                        .crossFade()
                        .into(mIconView);
            }
            // for accessibility, add a content description
            mIconView.setContentDescription(getString(R.string.a11y_forecast_icon, description));

            // Day/date
            long date = cursor.getLong(COL_WEATHER_DATE);
//            mFriendlyDateView.setText(Utility.getDayName(context, date));
            String dateText = Utility.getFullFriendlyDayString(context,date);
            mDateView.setText(dateText);

            // High and low temperature
            boolean isMetric = Utility.isMetric(getActivity());
            double highTemp = cursor.getDouble(COL_WEATHER_MAX_TEMP);
            String highTempString = Utility.formatTemperature(context, highTemp, isMetric);
            mHighTempView.setText(highTempString);
            // for accessibility, add a content description
            mHighTempView.setContentDescription(getString(R.string.a11y_high_temp, highTempString));

            double lowTemp = cursor.getDouble(COL_WEATHER_MIN_TEMP);
            String lowTempString = Utility.formatTemperature(context, lowTemp, isMetric);
            mLowTempView.setText(lowTempString);
            // for accessibility, add a content description
            mLowTempView.setContentDescription(getString(R.string.a11y_low_temp, lowTempString));

            // humidity
            float humidity = cursor.getFloat(COL_HUMIDITY);
            String humidityString = context.getString(R.string.format_humidity, humidity);
            mHumidityView.setText(humidityString);
            // for accessibility, add a content description
            mHumidityView.setContentDescription(getString(R.string.a11y_humidity, mHumidityView.getText()));
            mHumidityLabelView.setContentDescription(mHumidityView.getContentDescription());

            // wind
            float windSpeed = cursor.getFloat(COL_WIND_SPEED);
            float degrees = cursor.getFloat(COL_DEGREES);
            String windString = Utility.getFormattedWind(context, windSpeed, degrees, isMetric);
            mWindView.setText(windString);
            // for accessibility, add a content description
            mWindView.setContentDescription(getString(R.string.a11y_wind, mWindView.getText()));
            mWindLabelView.setContentDescription(mWindView.getContentDescription());

            // pressure
            float pressure = cursor.getFloat(COL_PRESSURE);
            String pressureString = getString(R.string.format_pressure, pressure);
            mPressureView.setText(pressureString);
            // for accessibility, add a content description
            mPressureView.setContentDescription(getString(R.string.a11y_pressure, mPressureView.getText()));
            mPressureLabelView.setContentDescription(mPressureView.getContentDescription());

            // Create a string for the sharing intent
            mForecastStr = convertCursorRowToUXFormat(cursor);
        }
//        if (mShareActionProvider != null ) {
//            mShareActionProvider.setShareIntent(createShareForecastIntent());
//        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (getView() != null) {
            Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);

            // We need to start the enter transition after the data has loaded
            if (activity instanceof DetailActivity) {
                activity.supportStartPostponedEnterTransition();

                if ( null != toolbarView ) {
                    activity.setSupportActionBar(toolbarView);
                    ActionBar actionBar = activity.getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.setDisplayShowTitleEnabled(false);
                        actionBar.setDisplayHomeAsUpEnabled(true);
                    }
                }
            } else {
                if (null != toolbarView) {
                    Menu menu = toolbarView.getMenu();
                    if (null != menu) {
                        menu.clear();
                    }
                    toolbarView.inflateMenu(R.menu.detailfragment);
                    finishCreatingMenu(toolbarView.getMenu());
                }
            }
        }
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(getActivity());
        return Utility.formatTemperature(getActivity(), high, isMetric) + "/"
                + Utility.formatTemperature(getActivity(), low, isMetric);
    }

    /*
     * This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
     * string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        String highAndLow = formatHighLows(
                cursor.getDouble(COL_WEATHER_MAX_TEMP),
                cursor.getDouble(COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(COL_WEATHER_DATE))
                + " - " + cursor.getString(COL_WEATHER_DESC)
                + " - " + highAndLow;
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
            // update the URI
            mForecastUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

}
