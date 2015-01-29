package com.bluewall.picturegame;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bluewall.picturegame.fragments.GameFragment;
import com.bluewall.picturegame.model.Question;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class GameActivity extends Activity {

    public static Question currentQuestion = new Question();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.con_frag);

        getFragmentManager().beginTransaction()
                .replace(R.id.container, new GameFragment())
                .commit();

    }
    public static void getQuestion(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("question");
        query.whereEqualTo("isActive", true);
        try {
            ParseObject object =  query.getFirst();
            currentQuestion.setQuestion(object.getString("question"));
            currentQuestion.setAnswer(object.getString("answer"));
            currentQuestion.setImage(object.getString("imageLink"));
            currentQuestion.setPlayerID(object.getString("playerID"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

       /* query.getFirstInBackground(new GetCallback<ParseObject>() {

            @Override
            public void done(ParseObject object, ParseException e) {
                if (object == null) {
                    // TODO: If we cant get the object try again ??
                    Log.i(TAG, "Couldnt pull down " + e);
                } else {
                    Log.i(TAG, " pull down " );
                    currentQuestion.setQuestion(object.getString("question"));
                    currentQuestion.setAnswer(object.getString("answer"));
                    currentQuestion.setImage(object.getString("imageLink"));
                    currentQuestion.setPlayerID(object.getString("playerID"));
                }
            }
        });*/

        Context context = MainActivity.context;
        CharSequence text = "Hello toast!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }





}
