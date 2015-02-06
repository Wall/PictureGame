package com.bluewall.picturegame.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bluewall.picturegame.MainActivity;
import com.bluewall.picturegame.R;
import com.bluewall.picturegame.com.bluewall.picturegame.utils.BitmapUtils;
import com.bluewall.picturegame.model.Constants;
import com.bluewall.picturegame.task.ImgurUploadTask;
import com.melnykov.fab.FloatingActionButton;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.bluewall.picturegame.R.color.primary;
import static com.bluewall.picturegame.R.drawable.*;

/**
 * Created by clazell on 22/01/2015.
 */
public class QuestionFragment extends Fragment {

    @InjectView(R.id.image)
    ImageView imageView;
    @InjectView(R.id.button)
    Button button;
    @InjectView(R.id.btn_image)
    ImageButton buttonImage;
    @InjectView(R.id.btn_question)
    ImageButton buttonQuestion;
    @InjectView(R.id.btn_answer)
    ImageButton buttonAnswer;
    @InjectView(R.id.image)
    ImageView image;
    @InjectView(R.id.text_view)
    TextView textView;
    @InjectView(R.id.edit_text)
    EditText editText;
    @InjectView(R.id.text_title)
    TextView textTitle;
    @InjectView(R.id.text_image)
    TextView txtImage;
    @InjectView(R.id.text_question)
    TextView txtQuestion;
    @InjectView(R.id.text_answer)
    TextView txtAnswer;
    @InjectView(R.id.btn_edit)
    FloatingActionButton buttonEdit;

    private static final int RC_GALLERY_IMAGE = 1001;
    private static final int RC_CAPTURE_IMAGE = 1002;
    String TAG = "Question Fragment";

    String imageLinkFromPhoto = "";
    private InputMethodManager imm;

    private enum Selected {IMAGE, QUESTION, ANSWER}

    //Track which item we are editing. Default is image.
    private Selected selectedCategory = Selected.IMAGE;

    //Hold the values entered in the editText
    private String questionValue = "";
    private String answer = "";


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
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "axis.otf");
        textTitle.setTypeface(font);
        ParsePush.subscribeInBackground("");
        //If the user has a question saved locally go straight to the game
        //otherwise put in a new question and save locally
        checkQuestionExists();

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        return rootView;
    }

    //do something with the image, eg. save to phone or imgur
    private void handleSelectedImage(Uri imageUri) {
        Uri tmp = BitmapUtils.resizeImage(Constants.MAX_IMAGE_WIDTH, Constants.MAX_IMAGE_HEIGHT, imageUri, getActivity());
        imageView.setImageURI(tmp);
        imageLinkFromPhoto = tmp.toString();
        imgurUploadTest(tmp);
        toggleViewColors();
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode,
                                 Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        switch (requestCode) {
            //The returned uri from capture or select can be used directly in the imgur upload if we want
            case RC_CAPTURE_IMAGE:
                Log.d(TAG, "onActivityResult: captured by camera");
                if (intent != null && intent.getData() != null) {
                    handleSelectedImage(intent.getData());
                }
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
            ParseObject question = new ParseObject(GameFragment.QUESTION_OBJECT_TAG);
            question.put("question", questionValue);
            question.put("answer", answer);
            question.put("imageLink", imageLinkFromPhoto);
            //question.put("isActive", true);
             question.put("playerID", MainActivity.getPlayerId());
            question.pinInBackground();

            MainActivity.hasQ = true;
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new GameFragment())
                    .commit();
        } else {
            Context context = MainActivity.context;
            CharSequence text = "You must fill out all fields!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    private boolean validate() {
        return !questionValue.isEmpty() && !answer.isEmpty() && !imageLinkFromPhoto.isEmpty();
    }

    //check whether the current player has a local saved question
    public void checkQuestionExists() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(GameFragment.QUESTION_OBJECT_TAG);
        query.fromLocalDatastore();
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object != null && MainActivity.hasQ) {
                    Log.i("check it ", "Q exists");
                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, new GameFragment())
                            .commit();
                }
            }
        });
    }

    //Use the camera to capture an image
    public void startImageCaptureIntent() {
        Intent intent = new Intent();
        intent.setAction("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent, RC_CAPTURE_IMAGE);
    }

    //Select an image from the gallery
    // @OnClick(R.id.button)
    public void startImageSelectIntent() {
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

    @OnClick(R.id.btn_image)
    public void buttonImageClicked() {
        image.setVisibility(View.VISIBLE);
        textView.setVisibility(View.GONE);
        editText.setVisibility(View.GONE);
        txtImage.setTextColor(getResources().getColor(R.color.secondary));
        txtQuestion.setTextColor(getResources().getColor(R.color.dark_gray));
        txtAnswer.setTextColor(getResources().getColor(R.color.dark_gray));
        buttonEdit.setImageResource(edit);
        selectedCategory = Selected.IMAGE;
    }

    @OnClick(R.id.btn_question)
    public void buttonQuestionClicked() {
        image.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);
        editText.setVisibility(View.GONE);
        txtQuestion.setTextColor(getResources().getColor(R.color.secondary));
        txtImage.setTextColor(getResources().getColor(R.color.dark_gray));
        txtAnswer.setTextColor(getResources().getColor(R.color.dark_gray));
        selectedCategory = Selected.QUESTION;
        displayEnteredTextInTextView();
        setViewsToDisplayTextMode();
    }

    @OnClick(R.id.btn_answer)
    public void buttonAnswerClicked() {
        image.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);
        editText.setVisibility(View.GONE);
        txtAnswer.setTextColor(getResources().getColor(R.color.secondary));
        txtImage.setTextColor(getResources().getColor(R.color.dark_gray));
        txtQuestion.setTextColor(getResources().getColor(R.color.dark_gray));
        selectedCategory = Selected.ANSWER;
        displayEnteredTextInTextView();
        setViewsToDisplayTextMode();
    }

    @OnClick(R.id.btn_edit)
    public void buttonEditClicked() {
        switch (selectedCategory) {
            case IMAGE:
                showSelectImageDialog();
                break;
            case QUESTION:
                saveTextAndToggleViews();
                break;
            case ANSWER:
                saveTextAndToggleViews();
                break;
        }

        toggleViewColors();
    }

    private void saveTextAndToggleViews() {
        if (editText.getVisibility() == View.VISIBLE) {
            //User just entered some value because editText is visible
            storeQuestionOrAnswer();
            //Set the textView to show new value
            displayEnteredTextInTextView();
            //Set editText to invisible and show textView instead
            setViewsToDisplayTextMode();
        } else {
            //Sets the editText to display previously entered text
            displaySavedTextInEditText();
            //Show edit mode so user can edit text
            setViewsToEditTextMode();
        }
    }

    private void setViewsToDisplayTextMode() {
        editText.setText("");
        editText.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);
        buttonEdit.setImageResource(edit);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private void setViewsToEditTextMode() {
        editText.setVisibility(View.VISIBLE);
        textView.setVisibility(View.GONE);
        buttonEdit.setImageResource(tick);
        editText.requestFocus();
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

    }

    private void displayEnteredTextInTextView() {
        if (selectedCategory == Selected.ANSWER) {
            if (!answer.isEmpty()) {
                textView.setText(answer);
            } else {
                textView.setText(getActivity().getResources().getString(R.string.empty_answer_field));
            }
        } else {
            if (!questionValue.isEmpty()) {
                textView.setText(questionValue);
            } else {
                textView.setText(getActivity().getResources().getString(R.string.empty_question_field));
            }
        }
    }

    private void displaySavedTextInEditText() {
        if (selectedCategory == Selected.ANSWER) {
            editText.setText(answer);
            editText.setHint(getActivity().getResources().getString(R.string.hint_answer));
        } else {
            editText.setText(questionValue);
            editText.setHint(getActivity().getResources().getString(R.string.hint_question));
        }
    }

    private void storeQuestionOrAnswer() {
        if (selectedCategory == Selected.ANSWER) {
            answer = editText.getText().toString();
        } else {
            questionValue = editText.getText().toString();
        }
    }

    /**
     * Sets the background colour of the three buttons
     */
    private void toggleViewColors() {
        if (!imageLinkFromPhoto.isEmpty()) {
            buttonImage.setBackgroundResource(green_circle);
        } else {
            buttonImage.setBackgroundResource(white_circle);
        }

        if (!questionValue.isEmpty()) {
            buttonQuestion.setBackgroundResource(green_circle);
        } else {
            buttonQuestion.setBackgroundResource(white_circle);
        }

        if (!answer.isEmpty()) {
            buttonAnswer.setBackgroundResource(green_circle);
        } else {
            buttonAnswer.setBackgroundResource(white_circle);
        }

        if (validate()) {
            button.setTextColor(getResources().getColor(R.color.background));
            button.setBackgroundResource(rectangle_rounded_green);
        } else {
            button.setTextColor(getResources().getColor(R.color.dark_gray));
            button.setBackgroundResource(rectangle_rounded);
        }
    }

    private void showSelectImageDialog() {
        new MaterialDialog.Builder(getActivity())
                .title("IMAGE")
                .content("Select an image to upload")
                .positiveText("Gallery")
                .neutralText("Cancel")
                .negativeText("Camera")
                .backgroundColorRes(R.color.background)
                .titleColorRes(R.color.primary)
                .neutralColorRes(R.color.secondary)
                .positiveColorRes(R.color.primary)
                .negativeColorRes(R.color.primary)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        startImageSelectIntent();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        startImageCaptureIntent();
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        //Cancel

                    }
                })
                .show();

    }
}
