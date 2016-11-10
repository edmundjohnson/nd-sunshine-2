package com.example.android.sunshine.app.gcm;

import com.google.android.gms.iid.InstanceIDListenerService;

import android.content.Intent;

/**
 * Service which handles the creation, rotation and updating of registration tokens.
 * @author Edmund Johnson
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {
    ///** Log tag for this class. */
    //private static final String TAG = "MyInstanceIDListenerService";

    /**
     * This method is called if InstanceID token is updated. This may occur if the security
     * of the previous token has been compromised.
     * This call is initiated by the InstanceID provider.
     */
    @Override
    public void onTokenRefresh() {
        // fetch updated InstanceID token
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }

}
