package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import static com.example.android.sunshine.app.ForecastFragment.COL_WEATHER_DATE;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.support.v7.widget.RecyclerView}.
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    private boolean mUseTodayLayout;

    private final Context mContext;
    private Cursor mCursor;
    private final ForecastAdapterOnClickHandler mClickHandler;
    private final View mEmptyListView;

    /**
     * Constructor.
     * @param context the context
     * @param clickHandler the item click handler
     * @param emptyListView the view to display if the list is empty
     */
    public ForecastAdapter(Context context, ForecastAdapterOnClickHandler clickHandler,
                           View emptyListView) {
        mContext = context;
        mClickHandler = clickHandler;
        mEmptyListView = emptyListView;
    }

//    /*
//     * These views are reused as needed.
//     */
//    @Override
//    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//        int viewType = getItemViewType(cursor.getPosition());
//        int layoutId;
//        switch (viewType) {
//            case VIEW_TYPE_TODAY:
//                layoutId = R.layout.list_item_forecast_today;
//                break;
//            case VIEW_TYPE_FUTURE_DAY:
//            default:
//                layoutId = R.layout.list_item_forecast;
//                break;
//        }
//        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
//        ViewHolder viewHolder = new ViewHolder(view);
//        view.setTag(viewHolder);
//        return view;
//    }

//    /*
//     * Write the contents of the cursor to the views.
//     */
//    @Override
//    public void bindView(View view, Context context, Cursor cursor) {
//        ViewHolder viewHolder = (ViewHolder) view.getTag();
//
//        // Icon
//        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
//        int defaultImage;
//        int viewType = getItemViewType(cursor.getPosition());
//        switch (viewType) {
//            case VIEW_TYPE_TODAY:
//                defaultImage = Utility.getArtResourceForWeatherCondition(weatherId);
//                break;
//            case VIEW_TYPE_FUTURE_DAY:
//            default:
//                defaultImage = Utility.getIconResourceForWeatherCondition(weatherId);
//                break;
//        }
//
//        if (Utility.usingLocalGraphics(mContext)) {
//            viewHolder.mIconView.setImageResource(defaultImage);
//        } else {
//            // Glide library call.
//            // .error(...) indicates a resource to be used if the load resource cannot be loaded
//            Glide.with(context)
//                    .load(Utility.getArtUrlForWeatherCondition(context, weatherId))
//                    .error(defaultImage)
//                    .crossFade()
//                    .into(viewHolder.mIconView);
//        }
//
//        // Date
//        long dateInMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
//        viewHolder.mDateView.setText(Utility.getFriendlyDayString(context, dateInMillis));
//
//        // Description
//        // TO DO: Use the weather id to get the description, so it can be internationalised
////        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
//        String description = Utility.getStringForWeatherCondition(context, weatherId);
//        viewHolder.mDescriptionView.setText(description);
//        // for accessibility, add a content description
//        viewHolder.mDescriptionView.setContentDescription(
//                context.getString(R.string.a11y_forecast, description));
//
//        // For accessibility, we don't want a content description for the icon field
//        // because the information is repeated in the description view and the icon
//        // is not individually selectable
//        // The next line is bad! It gets read out as "unlabelled".
//        //viewHolder.mIconView.setContentDescription(null);
//
//        // High temperature
//        boolean isMetric = Utility.isMetric(context);
//        double highTemp = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
//        String highTempString = Utility.formatTemperature(context, highTemp, isMetric);
//        viewHolder.mHighTempView.setText(highTempString);
//        // for accessibility, add a content description
//        viewHolder.mHighTempView.setContentDescription(
//                context.getString(R.string.a11y_high_temp, highTempString));
//
//        // Low temperature
//        double lowTemp = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
//        String lowTempString = Utility.formatTemperature(context, lowTemp, isMetric);
//        viewHolder.mLowTempView.setText(lowTempString);
//        // for accessibility, add a content description
//        viewHolder.mLowTempView.setContentDescription(
//                context.getString(R.string.a11y_low_temp, lowTempString));
//    }

//    @Override
//    public int getViewTypeCount() {
//        return 2;
//    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        this.mUseTodayLayout = useTodayLayout;
    }

//    public int getSelectedItemPosition() {
//        return getCursor().getPosition();
//    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    /**
     * Called when RecyclerView needs a new {@link ForecastAdapterViewHolder} of the given type
     * to represent an item.
     *
     * <p>This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     *
     * <p>The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(ForecastAdapterViewHolder, int)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param viewGroup The ViewGroup into which the new View will be added after it is bound to
     *                  an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(ForecastAdapterViewHolder, int)
     */
    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        if (viewGroup instanceof RecyclerView) {
            int layoutId;
            switch (viewType) {
                case VIEW_TYPE_TODAY:
                    layoutId = R.layout.list_item_forecast_today;
                    break;
                case VIEW_TYPE_FUTURE_DAY:
                default:
                    layoutId = R.layout.list_item_forecast;
                    break;
            }
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            View view = inflater.inflate(layoutId, viewGroup, false);
            view.setFocusable(true);
            return new ForecastAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link ForecastAdapterViewHolder#itemView} to reflect the item
     * at the given position.
     *
     * <p>Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link ForecastAdapterViewHolder#getAdapterPosition()}
     * which will have the updated adapter position.
     *
     * @param viewHolder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder viewHolder, int position) {
        Context context = mContext;

        mCursor.moveToPosition(position);

        // Icon
        int weatherId = mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int defaultImage;
//        int viewType = getItemViewType(mCursor.getPosition());
        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_TODAY:
                defaultImage = Utility.getArtResourceForWeatherCondition(weatherId);
                break;
            case VIEW_TYPE_FUTURE_DAY:
            default:
                defaultImage = Utility.getIconResourceForWeatherCondition(weatherId);
                break;
        }

        if (Utility.usingLocalGraphics(context)) {
            viewHolder.mIconView.setImageResource(defaultImage);
        } else {
            // Glide library call.
            // .error(...) indicates a resource to be used if the load resource cannot be loaded
            Glide.with(context)
                    .load(Utility.getArtUrlForWeatherCondition(context, weatherId))
                    .error(defaultImage)
                    .crossFade()
                    .into(viewHolder.mIconView);
        }

        // Date
        long dateInMillis = mCursor.getLong(COL_WEATHER_DATE);
        viewHolder.mDateView.setText(Utility.getFriendlyDayString(context, dateInMillis));

        // Description
        // Use the weather id to get the description, so it can be internationalised
        String description = Utility.getStringForWeatherCondition(context, weatherId);
        viewHolder.mDescriptionView.setText(description);
        // for accessibility, add a content description
        viewHolder.mDescriptionView.setContentDescription(
                context.getString(R.string.a11y_forecast, description));

        // For accessibility, we don't want a content description for the icon field
        // because the information is repeated in the description view and the icon
        // is not individually selectable
        // The next line is bad! It gets read out as "unlabelled".
        //viewHolder.mIconView.setContentDescription(null);

        // High temperature
        boolean isMetric = Utility.isMetric(context);
        double highTemp = mCursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        String highTempString = Utility.formatTemperature(context, highTemp, isMetric);
        viewHolder.mHighTempView.setText(highTempString);
        // for accessibility, add a content description
        viewHolder.mHighTempView.setContentDescription(
                context.getString(R.string.a11y_high_temp, highTempString));

        // Low temperature
        double lowTemp = mCursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        String lowTempString = Utility.formatTemperature(context, lowTemp, isMetric);
        viewHolder.mLowTempView.setText(lowTempString);
        // for accessibility, add a content description
        viewHolder.mLowTempView.setContentDescription(
                context.getString(R.string.a11y_low_temp, lowTempString));

        // Give each animated view a unique transition name. The Android animator can use
        // this to re-find the original view on the return transition.
        // This enables the animation to happen even when state is lost due to a device rotation.
        ViewCompat.setTransitionName(viewHolder.mIconView, "iconView" + position);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    /**
     * Return the cursor.
     * @return the cursor
     */
    public Cursor getCursor() {
        return mCursor;
    }

    /**
     * Swap in a new cursor.
     * @param newCursor the new cursor
     */
    public final void swapCursor(@Nullable final Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();

        int visibility = getItemCount() == 0 ? View.VISIBLE : View.GONE;
        mEmptyListView.setVisibility(visibility);
    }

//    /**
//     * Programmatically click on a view holder.
//     * @param viewHolder the view holder
//     */
//    public void selectView(RecyclerView.ViewHolder viewHolder) {
//        if (viewHolder instanceof ForecastAdapterViewHolder) {
//            ForecastAdapterViewHolder vfh = (ForecastAdapterViewHolder) viewHolder;
//            vfh.onClick(vfh.itemView);
//        }
//    }

    /**
     * Interface for the item click handler.
     */
    public interface ForecastAdapterOnClickHandler {
        void onClick(long date, ForecastAdapterViewHolder viewHolder);
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        public final ImageView mIconView;
        public final TextView mDateView;
        public final TextView mDescriptionView;
        public final TextView mHighTempView;
        public final TextView mLowTempView;

        /**
         * Constructor.
         * @param view the view corresponding to the viewHolder.
         */
        public ForecastAdapterViewHolder(final View view) {
            super(view);

            mIconView = (ImageView) view.findViewById(R.id.list_item_icon);
            mDateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            mDescriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            mHighTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            mLowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);

            long date = mCursor.getLong(COL_WEATHER_DATE);
            mClickHandler.onClick(date, this);

        }
    }

}
