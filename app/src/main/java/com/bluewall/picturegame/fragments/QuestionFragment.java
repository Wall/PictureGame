package com.bluewall.picturegame.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.bluewall.picturegame.MainActivity;
import com.bluewall.picturegame.R;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by clazell on 22/01/2015.
 */
public class QuestionFragment extends Fragment {

    @InjectView(R.id.button)
    Button button;

    @InjectView(R.id.editText2)
    EditText editQText;

    @InjectView(R.id.editText3)
    EditText editAText;

    @InjectView(R.id.editText4)
    EditText editLText;

    public QuestionFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.question_screen, container, false);
        ButterKnife.inject(this, rootView);

        //If the user has a question saved locally go straight to the game
        //otherwise put in a new question and save locally
        if (checkQuestionExists()){
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new GameFragment())
                    .commit();
        }

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return rootView;
    }

    @OnClick(R.id.button)
    public void submit(View view) {
        ParseObject question = new ParseObject("question");
        question.put("question",editQText.getText().toString());
        question.put("answer", editAText.getText().toString());

        // TODO: vvv When image capture is put in replace this hardcoded string vvv
        question.put("imageLink", "http://i.imgur.com/eJNnNUI.png");

        question.put("isActive",true);
        question.put("playerID",MainActivity.getPlayerId());

        question.pinInBackground();

        getFragmentManager().beginTransaction()
                .replace(R.id.container, new GameFragment())
                .commit();
    }

    //check whether the current player has a local saved question
    public boolean checkQuestionExists(){
        final boolean[] isTrue = {false};
        ParseQuery<ParseObject> query = ParseQuery.getQuery("question");
        query.fromLocalDatastore();
        query.getFirstInBackground( new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    isTrue[0] = true;
                } else {
                    isTrue[0] = false;
                }
            }
        });
        return isTrue[0];
    }
}
