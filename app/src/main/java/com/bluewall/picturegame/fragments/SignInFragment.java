package com.bluewall.picturegame.fragments;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

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

    @InjectView(R.id.text_title)
    TextView textTitle;
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

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "axis.otf");
        textTitle.setTypeface(font);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return rootView;
    }

    @OnClick(R.id.button_sign_in)
    public void submit(View view) {
        MainActivity.onSignInClick();
    }
    public static class DialogFragmentHowToPlay extends DialogFragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_how_to_play, container, false);

            // Watch for button clicks.
            Button button = (Button) v.findViewById(R.id.close);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // When button is clicked, call up to owning activity.
                    dismiss();
                }
            });
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            return v;
        }
    }


    @OnClick(R.id.btn_instructions)
    public void showInstructions(){
        DialogFragment newFragment = new DialogFragmentHowToPlay();
        newFragment.show(((FragmentActivity)getActivity()).getSupportFragmentManager(), "dialog");
    }

}