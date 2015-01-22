package com.bluewall.picturegame.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.bluewall.picturegame.MainActivity;
import com.bluewall.picturegame.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by clazell on 22/01/2015.
 */
public class SignInFragment extends Fragment {

    @InjectView(R.id.button_sign_in)
    com.google.android.gms.common.SignInButton logInButton;

    public SignInFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_main, container, false);
        ButterKnife.inject(this, rootView);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return rootView;
    }

    @OnClick(R.id.button_sign_in)
    public void submit(View view) {
        MainActivity.onSignInClick();
    }
}