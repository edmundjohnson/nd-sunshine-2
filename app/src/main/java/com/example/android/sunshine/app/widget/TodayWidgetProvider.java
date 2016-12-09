/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.example.android.sunshine.app.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

/**
 * Provider for a horizontally expandable widget showing today's weather.
 */
public class TodayWidgetProvider extends AppWidgetProvider {

    /**
     * Notification received that the widget has been launched (as broadcast from system).
     * @param context the context
     * @param appWidgetManager the widget manager
     * @param appWidgetIds the widget identifiers
     */
    @UiThread
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, TodayWidgetIntentService.class));
    }

    /**
     * Notification received that the widget configuration has changed, e.g. widget resized by user
     * (as broadcast from system).
     * @param context the context
     * @param appWidgetManager the widget manager
     * @param appWidgetId the widget identifier
     * @param newOptions the latest configuration options
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        context.startService(new Intent(context, TodayWidgetIntentService.class));
    }

    /**
     * Notification received that the data has changed (as broadcast from e.g. SunshineSyncAdapter).
     * @param context the context
     * @param intent the received intent
     */
    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (SunshineSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            context.startService(new Intent(context, TodayWidgetIntentService.class));
        }
    }

}
