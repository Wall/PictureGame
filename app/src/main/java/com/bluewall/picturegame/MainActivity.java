package com.bluewall.picturegame;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bluewall.picturegame.model.Question;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.plus.Plus;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener{

    final static String TAG = "Image Hunt";
    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // Client used to interact with Google APIs.
    private GoogleApiClient mGoogleApiClient;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean mAutoStartSignInFlow = false;

    @InjectView(R.id.button_sign_in)
    com.google.android.gms.common.SignInButton logInButton;

    @InjectView(R.id.retrievebutton)
    Button retrievebutton;

    @InjectView(R.id.pushbutton2)
    Button pushbutton2;

    @InjectView(R.id.editText)
    EditText editText;

    private View v;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.con_frag);

        //ButterKnife.inject(this);
        if (savedInstanceState == null) {

            getFragmentManager().beginTransaction()
                    .add(R.id.container, new QuestionFragment())
                    .commit();
        }
       // setContentView(R.layout.activity_main);


        // Create the Google Api Client with access to Plus and Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

       /* logInButton.setOnClickListener(this);
        retrievebutton.setOnClickListener(this);
        pushbutton2.setOnClickListener(this);
*/
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_id));

    }

    public static class QuestionFragment extends Fragment {

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

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return rootView;
    }


    }

    @OnClick({R.id.pushbutton2,  R.id.retrievebutton, R.id.button_sign_in })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pushbutton2:
                oncClick();
                break;
            case R.id.retrievebutton:
                onbClick();
                break;
            case R.id.button_sign_in:
                onSignInClick();
                break;
            default:
                break;
        }
    }

    public void oncClick() {
        Log.i(TAG, "pushbutton2 clicked");
        ParseObject question = new ParseObject("question");
        question.put("question", "How much wood would a woodchuck chuck if a woodchuck could chuck wood");
        question.put("answer", "none");
        question.put("imageLink", "www.google.com");

        question.saveInBackground();
    }

    public void onbClick() {
        Log.i(TAG, "onbClick clicked");
        ParseQuery<ParseObject> query = ParseQuery.getQuery("question");
        query.getInBackground("2WkA4eyX00", new GetCallback<ParseObject>() {
            public void done(ParseObject object, com.parse.ParseException e) {
                if (e == null) {

                    Question currentQuestion = new Question();
                    currentQuestion.setQuestion(object.getString("question"));
                    currentQuestion.setAnswer(object.getString("answer"));
                    currentQuestion.setImage(object.getString("imageLink"));

                    editText.setText(currentQuestion.getQuestion());

                } else {
                    Log.i(TAG,"Couldnt pull down");
                }
            }


        });

    }


    public void onSignInClick() {
                Log.i(TAG, "Sign-in button clicked");
                mSignInClicked = true;
                mGoogleApiClient.connect();
    }


    @Override
    public void onActivityResult(int requestCode, int responseCode,
                                 Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);

        switch (requestCode) {


            case RC_SIGN_IN:
                Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                        + responseCode + ", intent=" + intent);
                mSignInClicked = false;
                mResolvingConnectionFailure = false;
                if (responseCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                } else {
                    showActivityResultError(this, requestCode, responseCode, R.string.signin_other_error);
                }
                break;
        }
        super.onActivityResult(requestCode, responseCode, intent);
    }

    // Activity just got to the foreground. We switch to the wait screen because we will now
    // go through the sign-in flow (remember that, yes, every time the Activity comes back to the
    // foreground we go through the sign-in flow -- but if the user is already authenticated,
    // this flow simply succeeds and is imperceptible).
    @Override
    public void onStart() {

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.w(TAG,
                    "GameHelper: client was already connected on onStart()");
        } else {
            Log.d(TAG, "Connecting client.");
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

       /*
     * CALLBACKS SECTION. This section shows how we implement the several games
     * API callbacks.
     */

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected() called. Sign in successful!");

        Log.d(TAG, "Sign-in succeeded.");

        int REQUEST_LEADERBOARD = 100;

        Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderBoardID), 1337);

        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                getString(R.string.leaderBoardID)), REQUEST_LEADERBOARD);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = resolveConnectionFailure(this, mGoogleApiClient,
                    connectionResult, RC_SIGN_IN, getString(R.string.signin_other_error));
        }

     //   switchToScreen(R.id.screen_sign_in);
    }

    /*
     * GAME LOGIC SECTION. Methods that implement the game's rules.
     */
    public void getImage(){

    }
    public String getAnswer(){
        String correctAnswer ="";


        return correctAnswer;
    }

    public boolean checkAnswerCorrect(String answer){
        boolean isCorrect = false;


        return isCorrect;
    }

    /*
     * COMMUNICATIONS SECTION. Methods that implement the game's network
     * protocol.
     */


    // This array lists all the individual screens our game has.
    final static int[] SCREENS = {
            R.id.screen_sign_in,

    };
    int mCurScreen = -1;

    void switchToScreen(int screenId) {
        // make the requested screen visible; hide all others.
        for (int id : SCREENS) {
            findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
        }
        mCurScreen = screenId;


    }

    void switchToMainScreen() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
           // switchToScreen(R.id.screen_main);
        } else {
            switchToScreen(R.id.screen_sign_in);
        }
    }


    //GAME UTILS

    public static Dialog makeSimpleDialog(Activity activity, String text) {
        return (new AlertDialog.Builder(activity)).setMessage(text)
                .setNeutralButton(android.R.string.ok, null).create();
    }

    public static boolean resolveConnectionFailure(Activity activity,
                                                   GoogleApiClient client, ConnectionResult result, int requestCode,
                                                   String fallbackErrorMessage) {

        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(activity, requestCode);
                return true;
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                client.connect();
                return false;
            }
        } else {
            // not resolvable... so show an error message
            int errorCode = result.getErrorCode();
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
                    activity, requestCode);
            if (dialog != null) {
                dialog.show();
            } else {
                // no built-in dialog: show the fallback error message
                showAlert(activity, fallbackErrorMessage);
            }
            return false;
        }
    }

    public static void showAlert(Activity activity, String message) {
        (new AlertDialog.Builder(activity)).setMessage(message)
                .setNeutralButton(android.R.string.ok, null).create().show();
    }

    public static void showActivityResultError(Activity activity, int requestCode, int actResp, int errorDescription) {
        if (activity == null) {
            Log.e("BaseGameUtils", "*** No Activity. Can't show failure dialog!");
            return;
        }
        Dialog errorDialog;

        switch (actResp) {
            case GamesActivityResultCodes.RESULT_APP_MISCONFIGURED:
                errorDialog = makeSimpleDialog(activity,
                        activity.getString(R.string.app_misconfigured));
                break;
            case GamesActivityResultCodes.RESULT_SIGN_IN_FAILED:
                errorDialog = makeSimpleDialog(activity,
                        activity.getString(R.string.sign_in_failed));
                break;
            case GamesActivityResultCodes.RESULT_LICENSE_FAILED:
                errorDialog = makeSimpleDialog(activity,
                        activity.getString(R.string.license_failed));
                break;
            default:
                // No meaningful Activity response code, so generate default Google
                // Play services dialog
                final int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
                errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
                        activity, requestCode, null);
                if (errorDialog == null) {
                    // get fallback dialog
                    Log.e("BaseGamesUtils",
                            "No standard error dialog available. Making fallback dialog.");
                    errorDialog = makeSimpleDialog(activity, activity.getString(errorDescription));
                }
        }

        errorDialog.show();
    }
    /*
     * MISC SECTION. Miscellaneous methods.
     */


    // Sets the flag to keep this screen on. It's recommended to do that during
    // the
    // handshake when setting up a game, because if the screen turns off, the
    // game will be
    // cancelled.
    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


}
