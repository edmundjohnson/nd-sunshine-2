package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /*
     * These views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int layoutId;
        if (cursor.getPosition() == 0) {
            layoutId = R.layout.list_item_forecast_today;
        } else {
            layoutId = R.layout.list_item_forecast;
        }
        return LayoutInflater.from(context).inflate(layoutId, parent, false);
    }

    /*
     * Fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Date
        TextView txtDate = (TextView) view.findViewById(R.id.list_item_date_textview);
        long dateInMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        txtDate.setText(Utility.getFriendlyDayString(context, dateInMillis));

        // Description
        TextView txtDesc = (TextView) view.findViewById(R.id.list_item_forecast_textview);
        txtDesc.setText(cursor.getString(ForecastFragment.COL_WEATHER_DESC));

        // Icon
        ImageView imgWeather = (ImageView) view.findViewById(R.id.list_item_icon);
        imgWeather.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_launcher));

        // Max and min temperatures
        boolean isMetric = Utility.isMetric(context);
        double tempMax = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        TextView txtMaxTemp = (TextView) view.findViewById(R.id.list_item_high_textview);
        txtMaxTemp.setText(Utility.formatTemperature(tempMax, isMetric));
        double tempMin = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        TextView txtMinTemp = (TextView) view.findViewById(R.id.list_item_low_textview);
        txtMinTemp.setText(Utility.formatTemperature(tempMin, isMetric));
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_TODAY;
        } else {
            return VIEW_TYPE_FUTURE_DAY;

        }
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

}
