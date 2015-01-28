package com.bluewall.picturegame.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

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

        //TODO: UNCOMMENT THIS STUFF AFTER TESTING PUSHES
        //String REQUEST_LEADERBOARD = "100";


       // startActivityForResult(Games.Leaderboards.getLeaderboardIntent(MainActivity.getGoogleShiz(),
        //       getString(R.string.leaderBoardID)), 100);

        //updateLeaderboards(MainActivity.getGoogleShiz(),REQUEST_LEADERBOARD);


        ParsePush push = new ParsePush();
        push.setChannel("");
        push.setMessage("answer");
        push.sendInBackground();


        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return rootView;
    }

    @OnClick(R.id.backButton)
    public void back() {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new GameFragment())
                .commit();
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
