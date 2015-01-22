package com.bluewall.picturegame.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluewall.picturegame.MainActivity;
import com.bluewall.picturegame.R;
import com.bluewall.picturegame.model.Question;
import com.bluewall.picturegame.task.ImgurDownloadTask;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by clazell on 22/01/2015.
 */
public class GameFragment extends Fragment {

    @InjectView(R.id.AnswerButton)
    Button button;

    @InjectView(R.id.questionTextView)
    TextView questionText;

    @InjectView(R.id.imageView)
    ImageView imageView;

    @InjectView(R.id.editTextAnswer)
    EditText editTextAnswer;

    String TAG = "Game Fragment";

    Question currentQuestion;

    public GameFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.game_screen, container, false);
        ButterKnife.inject(this, rootView);

        //pull down the question set to active on the parse end
        ParseQuery<ParseObject> query = ParseQuery.getQuery("question");
        query.whereEqualTo("isActive", true);
        query.getFirstInBackground(new GetCallback<ParseObject>() {

            @Override
            public void done(ParseObject object, ParseException e) {
                if (object == null) {
                    // TODO: If we cant get the object try again ??
                    Log.i(TAG, "Couldnt pull down " + e);

                } else {
                    currentQuestion = new Question();
                    currentQuestion.setQuestion(object.getString("question"));
                    currentQuestion.setAnswer(object.getString("answer"));
                    currentQuestion.setImage(object.getString("imageLink"));
                    currentQuestion.setPlayerID(object.getString("playerID"));

                    questionText.setText(currentQuestion.getQuestion());
                    imgurDownloadTest(currentQuestion.getImage());
                }
            }
        });

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return rootView;
    }

    @OnClick(R.id.AnswerButton)
    public void submit(View view) {

        Log.i(TAG, editTextAnswer.getText().toString());
        Log.i(TAG, currentQuestion.getAnswer());

        // Make sure the player is not answering their own question
        if(!currentQuestion.getPlayerID().equals(MainActivity.getPlayerId())) {
            // TODO: Currently using a dirty equals for testing, will be updated with davids check alg.
            if (editTextAnswer.getText().toString().equals(currentQuestion.getAnswer())) {

                // TODO: vvv Not fleshed out yet vvv
                updateOldQuestion();

                // TODO: If correct, update the current Question to be non active and upload new question from users local data
                Log.i(TAG, "Correct");
                uploadNewQuestion();

                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new WinScreenFragment())
                        .commit();

               // MainActivity.increaseScore();
            }
        }else{
            Log.i(TAG, "Don't answer your own question ya pretzel");
        }

    }

    public void updateOldQuestion(){
        //TODO: update the past question to not active on parse end

        ParseQuery<ParseObject> query = ParseQuery.getQuery("question");
        query.whereEqualTo("isActive", true);
        query.getFirstInBackground(new GetCallback<ParseObject>() {

            @Override
            public void done(ParseObject object, ParseException e) {
                if (object == null) {
                    // TODO: If we cant get the object try again ??
                    Log.i(TAG, "Couldnt pull down " + e);

                } else {

                    object.put("isActive",false);
                    object.saveInBackground();
                }
            }
        });
    }

    public void uploadNewQuestion(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("question");
        query.fromLocalDatastore();
        query.getFirstInBackground( new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {

                    ParseObject question = new ParseObject("question");

                    question.put("question",object.getString("question"));
                    question.put("answer", object.getString("answer"));
                    question.put("imageLink", object.getString("imageLink"));//object.getString("imageLink")
                    question.put("isActive",true);
                    question.put("playerID",object.getString("playerID"));

                    question.saveInBackground();
                    question.unpinInBackground();

                } else {

                }
            }
        });

    }

    public void imgurDownloadTest(String url) {
        new ImgurDownloadTask(url) {
            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    Log.i(TAG, "Bitmap null");
                }
            }
        }.execute();
    }
}