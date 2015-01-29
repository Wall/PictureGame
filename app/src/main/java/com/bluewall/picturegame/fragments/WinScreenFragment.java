package com.bluewall.picturegame.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.bluewall.picturegame.MainActivity;
import com.bluewall.picturegame.R;
import com.bluewall.picturegame.TestBroadcastReciever;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.parse.ParseException;
import com.parse.ParsePush;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by clazell on 22/01/2015.
 */
public class WinScreenFragment extends Fragment {
    public WinScreenFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.win_screen, container, false);
        ButterKnife.inject(this, rootView);

       // Games.Leaderboards.submitScore(MainActivity.getGoogleShiz(), getString(R.string.leaderBoardID), +1);

        String REQUEST_LEADERBOARD = "100";


        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(MainActivity.getGoogleShiz(),
               getString(R.string.leaderBoardID)), 100);

        updateLeaderboards(MainActivity.getGoogleShiz(),REQUEST_LEADERBOARD);

        sendMessageAsIntent(rootView);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return rootView;
    }

    @OnClick(R.id.backButton)
    public void back() {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new GameFragment())
                .commit();
    }

    //Use JSON data to send a message to a broadcast receiver
    public void sendMessageAsIntent(View v)
    {
        JSONObject data = getJSONDataMessageForIntent();
        ParsePush push = new ParsePush();
        push.setChannel("");
        push.setData(data);
        push.sendInBackground();
    }
    //Notice how the 'action' attribute enables the
//broadcast receiver behavior.
    private JSONObject getJSONDataMessageForIntent()
    {
        try
        {
            JSONObject data = new JSONObject();
            //Notice alert is not required
            //data.put("alert", "Message from Intent");
            //instead action is used
            data.put("action", TestBroadcastReciever.ACTION);
            data.put("customdata", "custom data value");
            return data;
        }
        catch(JSONException x)
        {
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
