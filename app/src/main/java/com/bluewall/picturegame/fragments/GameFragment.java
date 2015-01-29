package com.bluewall.picturegame.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluewall.picturegame.GameActivity;
import com.bluewall.picturegame.MainActivity;
import com.bluewall.picturegame.R;
import com.bluewall.picturegame.model.Question;
import com.bluewall.picturegame.task.ImgurDownloadTask;
import com.bluewall.picturegame.view.InputText;
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
public class GameFragment extends Fragment {

    @InjectView(R.id.AnswerButton)
    Button button;

    @InjectView(R.id.questionTextView)
    TextView questionText;

    @InjectView(R.id.imageView)
    ImageView imageView;

    @InjectView(R.id.editTextAnswer)
    InputText editTextAnswer;

    //InputText inputText = (InputText) rootView.findViewById(R.id.inputText);

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

        GameActivity.getQuestion();
        setQ();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return rootView;
    }

   public void setQ(){

       questionText.setText(GameActivity.currentQuestion.getQuestion());
       editTextAnswer.setAnswer(GameActivity.currentQuestion.getAnswer());
       imgurDownloadTest(GameActivity.currentQuestion.getImage());
   }

    @OnClick(R.id.AnswerButton)
    public void submit(View view) {

        Log.i(TAG, editTextAnswer.getAnswer().toString());
        Log.i(TAG, GameActivity.currentQuestion.getAnswer());
        Log.i(TAG, "" + editTextAnswer.isCorrect());

        // Make sure the player is not answering their own question
        if (!GameActivity.currentQuestion.getPlayerID().equals(MainActivity.getPlayerId())) {

            if (editTextAnswer.isCorrect()) {
                updateOldQuestion();
                Log.i(TAG, "Correct");
                uploadNewQuestion();

                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new WinScreenFragment())
                        .commit();
            }
        } else {
            Log.i(TAG, "Don't answer your own question ya pretzel");
            new AlertDialog.Builder(getActivity()).setMessage("Don't answer your own question ya pretzel")
                    .setNeutralButton(android.R.string.ok, null).create().show();

            Context context = MainActivity.context;
            CharSequence text = "Don't answer your own question ya pretzel!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

    }

    // sets the old question to false and the next question to true
    public void updateOldQuestion() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("question");
        query.whereEqualTo("isActive", true);
        query.getFirstInBackground(new GetCallback<ParseObject>() {

            @Override
            public void done(ParseObject object, ParseException e) {
                if (object == null) {
                    // TODO: If we cant get the What do??
                    Log.i(TAG, "Couldnt pull down " + e);

                } else {

                    object.put("isActive", false);
                    object.saveInBackground();
                }
            }
        });

        ParseQuery<ParseObject> queryNext = ParseQuery.getQuery("question");
        queryNext.whereEqualTo("isNext", true);
        queryNext.getFirstInBackground(new GetCallback<ParseObject>() {

            @Override
            public void done(ParseObject object, ParseException e) {
                if (object == null) {
                    // TODO: If we cant get the What do??
                    Log.i(TAG, "Couldnt pull down " + e);

                } else {

                    object.put("isActive", true);
                    object.put("isNext", false);
                    object.saveInBackground();
                }
            }
        });
    }

    // upload the players locally saved question to be the next in queue question
    public void uploadNewQuestion() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("question");
        query.fromLocalDatastore();
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {

                    ParseObject question = new ParseObject("question");

                    question.put("question", object.getString("question"));
                    question.put("answer", object.getString("answer"));
                    question.put("imageLink", object.getString("imageLink"));//object.getString("imageLink")
                    question.put("isActive", false);
                    question.put("playerID", object.getString("playerID"));
                    question.put("isNext", true);

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