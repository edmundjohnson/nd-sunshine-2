package com.example.android.sunshine.app.ui;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.android.sunshine.app.MainActivity;
import com.example.android.sunshine.app.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Espresso test.
 * @author Edmund Johnson
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ForecastListToDetailTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testSelectForecast() {
        // Note: this test fails on tablets
        // we're not displaying the detail view yet
        onView(withId(R.id.detail_date_textview))
                .check(doesNotExist());

        // we are displaying the forecast list
        onView(withId(R.id.recyclerview_forecast))
                .check(matches(isDisplayed()));

//        // NO! THIS IS FOR A ListView
//        // click on today's forecast list item
//        onData(anything())
//                .inAdapterView(withId(R.id.recyclerview_forecast))
//                .atPosition(0)
//                .perform(click());

        // For RecyclerView:
        // click on today's forecast list item
        onView(withId(R.id.recyclerview_forecast))
                .perform(
                    RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // we're displaying the detail view now!
        onView(withId(R.id.detail_date_textview))
                .check(matches(isDisplayed()));
    }
}
