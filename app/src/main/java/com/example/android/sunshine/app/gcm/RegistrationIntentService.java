package com.example.android.sunshine.app.gcm;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.android.sunshine.app.MainActivity;
import com.example.android.sunshine.app.R;

/**
 * Google Cloud Messaging helper service.
 * @author Edmund Johnson
 */
public class RegistrationIntentService extends IntentService {
    /** Tag for this class. */
    private static final String TAG = RegistrationIntentService.class.getSimpleName();

    public RegistrationIntentService() {
        super(TAG);
    }

    /**
     * Get the token and save it in SharedPreferences.
     * @param intent the intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                // Initially this call goes out to the network to retrieve the token,
                // subsequent calls are local.
                InstanceID instanceID = InstanceID.getInstance(this);

                // TODO: gcm_default sender ID comes from the API console
                String senderId = getString(R.string.gcm_defaultSenderId);
                if (senderId.length() > 0) {
                    String token = instanceID.getToken(senderId,
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    sendRegistrationToServer(token);
                }

                // You should store a boolean that indicates whether the generated token has been
                // sent to your server. If the boolean is false, send the token to your server,
                // otherwise your server should have already received the token.
                sharedPreferences.edit().
                        putBoolean(MainActivity.SENT_TOKEN_TO_SERVER, true).apply();
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh");

            sharedPreferences.edit().
                    putBoolean(MainActivity.SENT_TOKEN_TO_SERVER, false).apply();
        }
    }

    /**
     * Normally, you would want to persist the registration to third-party servers. Because we do
     * not have a server, and are faking it with a website, you'll want to log the token instead.
     * That way you can see the value in logcat, and note it for future use in the website.
     *
     * @param token the new token
     */
    private void sendRegistrationToServer(String token) {
        Log.i(TAG, "GCM Registration Token: " + token);
    }

}