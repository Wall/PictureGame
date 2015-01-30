package com.bluewall.picturegame.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.bluewall.picturegame.CustomParsePushBroadcastReciever;
import com.bluewall.picturegame.MainActivity;
import com.bluewall.picturegame.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by clazell on 22/01/2015.
 */
public class WinScreenFragment extends Fragment {
    String REQUEST_LEADERBOARD = "100";
    public WinScreenFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.win_screen, container, false);
        ButterKnife.inject(this, rootView);

        //Unsubscribe from the push broadcast channel so the winning client does not
        // receive the push to refresh game fragment.
        ParsePush.unsubscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.i("com.parse.push", "successfully unsubscribed to the  channel.");
                    sendMessageAsIntent(rootView);

                } else {
                    Log.i("com.parse.push", "failed to unsubscribe for push", e);
                }
            }
        });
        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(MainActivity.getGoogleShiz(),
                getString(R.string.leaderBoardID)), 100);

        updateLeaderboards(MainActivity.getGoogleShiz(),REQUEST_LEADERBOARD);

        //Send the parse push to update all other devices


        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return rootView;
    }

    @OnClick(R.id.backButton)
    public void back() {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new QuestionFragment())
                .commit();
    }

    //Use JSON data to send a message to the custom broadcast receiver
    //on the "" channel
    public void sendMessageAsIntent(View v){
        JSONObject data = getJSONDataMessageForIntent();
        ParsePush push = new ParsePush();
        push.setChannel("");
        push.setData(data);
        push.sendInBackground();
    }
    //Notice how the 'action' attribute enables the
//broadcast receiver behavior.
    private JSONObject getJSONDataMessageForIntent(){
        try{
            JSONObject data = new JSONObject();
            data.put("action", CustomParsePushBroadcastReciever.ACTION);
            data.put("customdata", "custom data value");
            return data;
        }
        catch(JSONException x){
            throw new RuntimeException("Something wrong with JSON", x);
        }
    }

    private static void updateLeaderboards(final GoogleApiClient googleApiClient, final String leaderboardId) {
        Games.Leaderboards.loadCurrentPlayerLeaderboardScore(
                googleApiClient,
                leaderboardId,
                LeaderboardVariant.TIME_SPAN_ALL_TIME,
                LeaderboardVariant.COLLECTION_PUBLIC
        ).setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {

            @Override
            public void onResult(Leaderboards.LoadPlayerScoreResult loadPlayerScoreResult) {
                if (loadPlayerScoreResult != null) {
                    if (GamesStatusCodes.STATUS_OK == loadPlayerScoreResult.getStatus().getStatusCode()) {
                        long score = 0;
                        if (loadPlayerScoreResult.getScore() != null) {
                            score = loadPlayerScoreResult.getScore().getRawScore();
                        }
                        Games.Leaderboards.submitScore(googleApiClient, leaderboardId, ++score);
                    }
                }
            }

        });

        // startActivityForResult(Games.Leaderboards.getLeaderboardIntent(MainActivity.getGoogleShiz(),
        //         getString(R.string.leaderBoardID)), REQUEST_LEADERBOARD);


    }

}
