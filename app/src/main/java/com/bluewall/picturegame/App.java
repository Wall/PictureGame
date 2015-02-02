package com.bluewall.picturegame;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;

/**
 * Created by clazell on 2/02/2015.
 */
public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_id));
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
