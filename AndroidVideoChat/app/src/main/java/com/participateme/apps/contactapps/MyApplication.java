package com.participateme.apps.contactapps;

/**
 * Created by dharamvir on 25/02/2018.
 */

import android.app.Application;
import com.google.android.gms.ads.MobileAds;

/**
 * Created by ravi on 25/12/17.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize the AdMob app
        MobileAds.initialize(this, getString(R.string.app_id));
    }
}
