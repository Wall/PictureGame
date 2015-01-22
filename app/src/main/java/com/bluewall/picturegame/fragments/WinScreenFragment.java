package com.bluewall.picturegame.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.bluewall.picturegame.MainActivity;
import com.bluewall.picturegame.R;
import com.google.android.gms.games.Games;

import butterknife.ButterKnife;

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

        Games.Leaderboards.submitScore(MainActivity.getGoogleShiz(), getString(R.string.leaderBoardID), 1);

        int REQUEST_LEADERBOARD = 100;

        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(MainActivity.getGoogleShiz(),
                getString(R.string.leaderBoardID)), REQUEST_LEADERBOARD);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return rootView;
    }

}
