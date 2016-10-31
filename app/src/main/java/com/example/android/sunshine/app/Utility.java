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
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.format.Time;

import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utility {

    /**
     * Format used for storing dates in the database.  Also used for converting those strings
     * back into date objects for comparison/processing.
     */
    //public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Returns whether the device is connected to the internet.
     * @param context the context
     * @return true if the device is connected to the internet, false otherwise
     */
    public static boolean isNetworkConnection(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    /**
     * Returns the user's location status.
     * @param context the current context
     * @return the user's location status
     */
    public static @SunshineSyncAdapter.LocationStatus int getLocationStatus(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        @SunshineSyncAdapter.LocationStatus int locationStatus =
                prefs.getInt(context.getString(R.string.pref_location_status_key),
                        SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN);
        return locationStatus;
    }

    /**
     * Sets the user's location status.
     * @param context the current context
     * @param locationStatus the new location status
     * @param foreground set this true if calling this method on a UI thread, false otherwise
     */
    public static void setLocationStatus(Context context,
                    @SunshineSyncAdapter.LocationStatus int locationStatus, boolean foreground) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor spe = prefs.edit();
        spe.putInt(context.getString(R.string.pref_location_status_key), locationStatus);
        // On background thread, use commit(); on foreground thread, use apply()
        if (foreground) {
            spe.apply();
        } else {
            spe.commit();
        }
    }

    /**
     * Resets the user's location status to Unknown.
     * @param context the current context
     */
    public static void resetLocationStatus(Context context, boolean foreground) {
        setLocationStatus(context, SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN, foreground);
    }

    /**
     * Returns the user's location setting.
     * @param context the current context
     * @return the user's location setting
     */
    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String location = prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
        return location.isEmpty() ? context.getString(R.string.pref_location_default) : location;
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric)).
                    equals(context.getString(R.string.pref_units_metric));
    }

    @NonNull
    private static String getIconPref(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_art_pack_key),
                context.getString(R.string.pref_art_pack_sunshine));
    }

    public static String formatTemperature(Context context, double celsiusTemperature) {
        return formatTemperature(context, celsiusTemperature, isMetric(context));
    }

    public static String formatTemperature(Context context, double celsiusTemperature, boolean isMetric) {
        double temperature;
        if (isMetric) {
            temperature = celsiusTemperature;
        } else {
            temperature = 9 * celsiusTemperature / 5 + 32;
        }
        return context.getString(R.string.format_temperature, temperature);
    }

    public static String getFormattedWind(Context context, float windSpeed, float degrees, boolean isMetric) {
        int windFormat;
        if (isMetric) {
            windFormat = R.string.format_wind_kmh;
        } else {
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        // From wind direction in degrees, determine compass direction as a string (e.g NW)
        // You know what's fun, writing really long if/else statements with tons of possible
        // conditions.  Seriously, try it!
        String direction = "Unknown";
        if (degrees >= 337.5 || degrees < 22.5) {
            direction = "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "W";
        } else if (degrees >= 292.5 || degrees < 22.5) {
            direction = "NW";
        }
        return String.format(context.getString(windFormat), windSpeed, direction);
    }

    public static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }

    public static String getFriendlyDayString(Context context, long dateInMillis) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Time time = new Time();
        time.setToNow();
        long currentTime = System.currentTimeMillis();
        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (julianDay == currentJulianDay) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return context.getString(
                    formatId,
                    today,
                    getFormattedMonthDay(context, dateInMillis));
        } else if ( julianDay < currentJulianDay + 7 ) {
            // If the input date is less than a week in the future, just return the day name.
            return getDayName(context, dateInMillis);
        } else {
            // Otherwise, use the form "Mon Jun 3"
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd", Locale.getDefault());
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return a user-friendly representation of the date.
     */
    public static String getFullFriendlyDayString(Context context, long dateInMillis) {

        String day = getDayName(context, dateInMillis);
        int formatId = R.string.format_full_friendly_date;
//        return String.format(context.getString(
//                formatId,
//                day,
//                getFormattedMonthDay(context, dateInMillis)));
        return context.getString(
                formatId,
                day,
                getFormattedMonthDay(context, dateInMillis));
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context context to use for resource localization
     * @param dateInMillis the date in milliseconds
     * @return the day description for the date
     */
    private static String getDayName(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.

        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if (julianDay == currentJulianDay + 1) {
            return context.getString(R.string.tomorrow);
        } else {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday").
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
            return dayFormat.format(dateInMillis);
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     * @param context Context to use for resource localization
     * @param dateInMillis The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    private static String getFormattedMonthDay(Context context, long dateInMillis ) {
        Time time = new Time();
        time.setToNow();
        //SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT, Locale.getDefault());
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd", Locale.getDefault());
        return monthDayFormat.format(dateInMillis);
    }

    /**
     * Helper method to return whether or not Sunshine is using local graphics.
     *
     * @param context Context to use for retrieving the preference
     * @return true if Sunshine is using local graphics, false otherwise.
     */
    public static boolean usingLocalGraphics(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String sunshineArtPack = context.getString(R.string.pref_art_pack_sunshine);
        return prefs.getString(context.getString(R.string.pref_art_pack_key),
                sunshineArtPack).equals(sunshineArtPack);
    }

    /**
     * Return the weather icon resource corresponding to a weatherId.
     * @param weatherId the weather id
     * @return the weather icon resource corresponding to the weatherId
     */
    public static int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return -1;
    }

    /**
     * Return the weather art image corresponding to a weatherId.
     * @param weatherId the weather id
     * @return the weather art image corresponding to a weatherId
     */
    public static int getArtResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
    }

    /**
     * Return the URL for the weather art image corresponding to a weatherId.
     * @param context the context
     * @param weatherId the weather id
     * @return the URL for the weather art image corresponding to a weatherId
     */
    public static String getArtUrlForWeatherCondition(Context context, int weatherId) {
        String weatherCode = null;
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            weatherCode = "storm";
        } else if (weatherId >= 300 && weatherId <= 321) {
            weatherCode = "light_rain";
        } else if (weatherId >= 500 && weatherId <= 504) {
            weatherCode = "rain";
        } else if (weatherId == 511) {
            weatherCode = "snow";
        } else if (weatherId >= 520 && weatherId <= 531) {
            weatherCode = "rain";
        } else if (weatherId >= 600 && weatherId <= 622) {
            weatherCode = "snow";
        } else if (weatherId >= 701 && weatherId <= 761) {
            weatherCode = "fog";
        } else if (weatherId == 761 || weatherId == 781) {
            weatherCode = "storm";
        } else if (weatherId == 800) {
            weatherCode = "clear";
        } else if (weatherId == 801) {
            weatherCode = "light_clouds";
        } else if (weatherId >= 802 && weatherId <= 804) {
            weatherCode = "clouds";
        }
        String formatArtUrl = getFormatArtUrl(context);
        if (weatherCode != null && formatArtUrl != null) {
            return String.format(Locale.US, formatArtUrl, weatherCode);
        }
        return null;
    }

    private static String getFormatArtUrl(Context context) {
        String iconPref = getIconPref(context);
        if (iconPref.equals(context.getString(R.string.pref_art_pack_sunshine))) {
            return context.getString(R.string.format_art_url_1);
        } else if (iconPref.equals(context.getString(R.string.pref_art_pack_cute_dogs))) {
            return context.getString(R.string.format_art_url_2);
        } else {
            return null;
        }
    }

    /**
     * Helper method to provide the string according to the weather
     * condition id returned by the OpenWeatherMap call.
     * @param context Android context
     * @param weatherId from OpenWeatherMap API response
     * @return string for the weather condition. null if no relation is found.
     */
    public static String getStringForWeatherCondition(Context context, int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        int stringId;
        if (weatherId >= 200 && weatherId <= 232) {
            stringId = R.string.condition_2xx;
        } else if (weatherId >= 300 && weatherId <= 321) {
            stringId = R.string.condition_3xx;
        } else {
            switch (weatherId) {
                case 500:
                    stringId = R.string.condition_500;
                    break;
                case 501:
                    stringId = R.string.condition_501;
                    break;
                case 502:
                    stringId = R.string.condition_502;
                    break;
                case 503:
                    stringId = R.string.condition_503;
                    break;
                case 504:
                    stringId = R.string.condition_504;
                    break;
                case 511:
                    stringId = R.string.condition_511;
                    break;
                case 520:
                    stringId = R.string.condition_520;
                    break;
                case 531:
                    stringId = R.string.condition_531;
                    break;
                case 600:
                    stringId = R.string.condition_600;
                    break;
                case 601:
                    stringId = R.string.condition_601;
                    break;
                case 602:
                    stringId = R.string.condition_602;
                    break;
                case 611:
                    stringId = R.string.condition_611;
                    break;
                case 612:
                    stringId = R.string.condition_612;
                    break;
                case 615:
                    stringId = R.string.condition_615;
                    break;
                case 616:
                    stringId = R.string.condition_616;
                    break;
                case 620:
                    stringId = R.string.condition_620;
                    break;
                case 621:
                    stringId = R.string.condition_621;
                    break;
                case 622:
                    stringId = R.string.condition_622;
                    break;
                case 701:
                    stringId = R.string.condition_701;
                    break;
                case 711:
                    stringId = R.string.condition_711;
                    break;
                case 721:
                    stringId = R.string.condition_721;
                    break;
                case 731:
                    stringId = R.string.condition_731;
                    break;
                case 741:
                    stringId = R.string.condition_741;
                    break;
                case 751:
                    stringId = R.string.condition_751;
                    break;
                case 761:
                    stringId = R.string.condition_761;
                    break;
                case 762:
                    stringId = R.string.condition_762;
                    break;
                case 771:
                    stringId = R.string.condition_771;
                    break;
                case 781:
                    stringId = R.string.condition_781;
                    break;
                case 800:
                    stringId = R.string.condition_800;
                    break;
                case 801:
                    stringId = R.string.condition_801;
                    break;
                case 802:
                    stringId = R.string.condition_802;
                    break;
                case 803:
                    stringId = R.string.condition_803;
                    break;
                case 804:
                    stringId = R.string.condition_804;
                    break;
                case 900:
                    stringId = R.string.condition_900;
                    break;
                case 901:
                    stringId = R.string.condition_901;
                    break;
                case 902:
                    stringId = R.string.condition_902;
                    break;
                case 903:
                    stringId = R.string.condition_903;
                    break;
                case 904:
                    stringId = R.string.condition_904;
                    break;
                case 905:
                    stringId = R.string.condition_905;
                    break;
                case 906:
                    stringId = R.string.condition_906;
                    break;
                case 951:
                    stringId = R.string.condition_951;
                    break;
                case 952:
                    stringId = R.string.condition_952;
                    break;
                case 953:
                    stringId = R.string.condition_953;
                    break;
                case 954:
                    stringId = R.string.condition_954;
                    break;
                case 955:
                    stringId = R.string.condition_955;
                    break;
                case 956:
                    stringId = R.string.condition_956;
                    break;
                case 957:
                    stringId = R.string.condition_957;
                    break;
                case 958:
                    stringId = R.string.condition_958;
                    break;
                case 959:
                    stringId = R.string.condition_959;
                    break;
                case 960:
                    stringId = R.string.condition_960;
                    break;
                case 961:
                    stringId = R.string.condition_961;
                    break;
                case 962:
                    stringId = R.string.condition_962;
                    break;
                default:
                    return context.getString(R.string.condition_unknown, weatherId);
            }
        }
        return context.getString(stringId);
    }

}
