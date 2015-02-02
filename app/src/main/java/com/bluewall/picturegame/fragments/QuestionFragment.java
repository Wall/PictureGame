package com.bluewall.picturegame.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bluewall.picturegame.MainActivity;
import com.bluewall.picturegame.R;
import com.bluewall.picturegame.com.bluewall.picturegame.utils.BitmapUtils;
import com.bluewall.picturegame.model.Constants;
import com.bluewall.picturegame.task.ImgurUploadTask;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import java.util.List;

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

    private static final int RC_GALLERY_IMAGE = 1001;
    private static final int RC_CAPTURE_IMAGE = 1002;

    String imageLinkFromPhoto = "";

    String TAG = "Question Fragment";

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
        ParsePush.subscribeInBackground("");
        //If the user has a question saved locally go straight to the game
        //otherwise put in a new question and save locally
        checkQuestionExists();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        return rootView;
    }

    //do something with the image, eg. save to phone or imgur
    private void handleSelectedImage(Uri imageUri) {
        Uri tmp = BitmapUtils.resizeImage(Constants.MAX_IMAGE_WIDTH,Constants.MAX_IMAGE_HEIGHT,imageUri,getActivity());
        imageLinkFromPhoto = tmp.toString();
        imgurUploadTest(tmp);
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode,
                                 Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        switch (requestCode) {
            //The returned uri from capture or select can be used directly in the imgur upload if we want
            case RC_CAPTURE_IMAGE:
                Log.d(TAG, "onActivityResult: captured by camera");
                handleSelectedImage(intent.getData());
            case RC_GALLERY_IMAGE:
                //Do something with the returned image
                if (intent != null && intent.getData() != null) {
                    Log.d(TAG, "onActivityResult: selected image uri =" + intent.getData().toString());
                 
                } else {
                    Log.d(TAG, "onActivityResult: no image selected or returned");
                }
                break;
        }
        super.onActivityResult(requestCode, responseCode, intent);
    }

    public void imgurUploadTest(Uri imageUri) {
        new ImgurUploadTask(imageUri, getActivity()) {
            @Override
            protected void onPostExecute(String id) {
                String url = "http://i.imgur.com/" + id + ".jpg";
                imageLinkFromPhoto = "http://i.imgur.com/" + id + ".jpg";
                System.out.println("URL: " + url);
                //imgurDownloadTest(url);
            }
        }.execute();
    }

    @OnClick(R.id.button)
    public void submit(View view) {
        if (validate()) {
            ParseObject question = new ParseObject("question");
            question.put("question", editQText.getText().toString());
            question.put("answer", editAText.getText().toString());
            question.put("imageLink", imageLinkFromPhoto);
            //question.put("isActive", true);
            //TODO: uncomment
           // question.put("playerID", MainActivity.getPlayerId());
            question.pinInBackground();

            MainActivity.hasQ = true;
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new GameFragment())
                    .commit();
        }

        else {
            Context context = MainActivity.context;
            CharSequence text = "You must fill out all fields!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    private boolean validate() {
        return !editQText.getText().toString().isEmpty() && !editAText.getText().toString().isEmpty() && !imageLinkFromPhoto.isEmpty();
    }

    //check whether the current player has a local saved question
    public void checkQuestionExists() {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("question");
        query.fromLocalDatastore();
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object != null && MainActivity.hasQ) {
                    Log.i("check it ","Q exists");
                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, new GameFragment())
                            .commit();
                }
            }
        });

    }

    //Use the camera to capture an image
    @OnClick(R.id.button_capture_image)
    public void startImageCaptureIntent() {
        Intent intent = new Intent();
        intent.setAction("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent, RC_CAPTURE_IMAGE);
    }

    //Select an image from the gallery
    @OnClick(R.id.button_select_image)
    public void startImageSelectIntent(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image*//*");

        List<ResolveInfo> list = getActivity().getPackageManager()
                .queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() <= 0) {
            Log.d(TAG, "no photo picker intent on this hardware");
            return;
        }
        startActivityForResult(intent, RC_GALLERY_IMAGE);
    }
}
