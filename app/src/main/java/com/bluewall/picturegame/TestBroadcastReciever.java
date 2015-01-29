package com.bluewall.picturegame;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;

/**
 * Created by clazell on 29/01/2015.
 */
public class TestBroadcastReciever
        extends BroadcastReceiver {
    public static final String ACTION = "com.androidbook.parse.TestPushAction";
    public static final String PARSE_EXTRA_DATA_KEY = "com.parse.Data";
    public static final String PARSE_JSON_ALERT_KEY = "alert";
    public static final String PARSE_JSON_CHANNELS_KEY = "com.parse.Channel";

    private static final String TAG = "TestBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();

            //"com.parse.Channel"
            String channel =
                    intent.getExtras()
                            .getString(PARSE_JSON_CHANNELS_KEY);

            JSONObject json =
                    new JSONObject(
                            intent.getExtras()
                                    .getString(PARSE_EXTRA_DATA_KEY));


            Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
            Iterator itr = json.keys();
            while (itr.hasNext()) {
                String key = (String) itr.next();
                Log.d(TAG, "..." + key + " => " + json.getString(key));
            }
            intent = new Intent(context,GameActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             context.startActivity(intent);
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }
    }
}