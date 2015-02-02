package com.bluewall.picturegame.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

/**
 * Created by dlee on 22/01/2015.
 */
public class InputText extends EditText implements View.OnFocusChangeListener{

    public static final String TAG = "InputText";

    /* List of all characters allowed in this inputText */
    private static final String VALID_CHARACTERS =
            "abcdefghijklmnopqrstuvwxyz" +
            "ABCDEFGHIJKLMNOPQRSTUVQXYZ" +
            "0123456789" +
            ",.<>?:;+=-" +
            "!@#$%^&*()`~";

    /* Number of non space characters in the answer */
    private int numCharacters;

    /* The actual answer (only the answer structure is necessary)
     * e.g. if the answer is "cam sucks hard", then "aaa aaaaa aaaa" is also a valid value */
    private String answer;

    /* The String that gets displayed in the EditText */
    private String visibleAnswer;

    /* The current answer that the user has inputted thus far */
    private StringBuilder currentAnswer = new StringBuilder();

    /*************** CONSTRUCTORS ***************/
    public InputText(Context context) {
        super(context);
        init(context);
    }

    public InputText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public InputText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    /********************************************/

    private void init(Context context) {
        Context context1 = context;
        setCursorVisible(false);
        setBackgroundDrawable(null);
        //setTypeface(Typeface.MONOSPACE);

        addTextChangedListener(new TextWatcher() {

            private int preLength;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                preLength = s.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //Set Cursor to end of text
                setSelection(s.length());
                //TextWatcher methods are called after every text change, below avoids recursive calls to this method as a result of this method changing the text itself
                if (!s.toString().equals(visibleAnswer)) {
                    int cursorPosition = getSelectionStart();
                    // cursorPosition == 0 implies there is no text to begin with
                    if (cursorPosition > 0) {
                        // Checks if the user pressed <backspace>
                        if (s.length() < preLength) {
                            if (currentAnswer.length() > 0) {
                                // Remove trailing character in current answer
                                currentAnswer.setLength(currentAnswer.length() - 1);
                            }
                        } else {
                            // Checks if user has already filled up the inputView
                            if (currentAnswer.length() < numCharacters) {
                                //Extract the trailing character and append it to currentAnswer
                                char c = s.charAt(s.length() - 1);
                                if (isValidChar(c)) {
                                    currentAnswer.append(c);
                                }
                            }
                        }
                        // Rebuild the visible answer based on changes made to currentAnswer above
                        buildVisibleAnswer();
                        InputText.this.setText(visibleAnswer);
                    }
                }
            }

            private boolean isValidChar(char c) {
                return VALID_CHARACTERS.indexOf(c) != -1;
            }

        });
    }

    /**
     * Set the answer for the InputBox (only the answer structure is necessary)
     * e.g. if the answer is "cam sucks hard", then "aaa aaaaa aaaa" is also a valid value
     * @param answer
     */
    public void setAnswer(String answer) {
        // Removes leading and trailing whitespace and sets everything to lower case
        this.answer = answer.trim().toLowerCase();
        // Builds the text to be written to screen
        buildVisibleAnswer();
        this.setText(visibleAnswer);

        // Counts the number of non-whitespace characters in string
        int numSpaces = 0;
        for (int i = 0; i < answer.length(); ++i) {
            if (answer.charAt(i) == ' ') {
                ++numSpaces;
            }
        }
        numCharacters = answer.length() - numSpaces;
    }

    /**
     * Returns current answer as inputted by user
     * @return
     */
    public String getAnswer() {
        return currentAnswer.toString();
    }

    /**
     * Returns whether the inputted answer is correct
     * @return
     */
    public boolean isCorrect() {
        return answer.replace(" ", "").equalsIgnoreCase(currentAnswer.toString());
    }

    /*
     * Builds the visible answer to be displayed
     */
    private void buildVisibleAnswer() {
        StringBuilder visible = new StringBuilder();

        int count = 0;
        //Iterate over chars in string
        for (int i = 0; i < answer.length(); ++i) {
            if (answer.charAt(i) == ' ') {
                visible.append("  ");
            } else {
                // Append char or _ depending on what user has inputted thus far
                if (count < currentAnswer.length()) {
                    visible.append(" " + currentAnswer.charAt(count) + " ");
                } else {
                    visible.append(" _ ");
                }
                //Keep running count on non-space characters
                ++count;
            }
        }
        visibleAnswer = visible.toString();
    }

    /*
     * Set cursor to end when first selecting View
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);
        setSelection(getText().length());
        return ret;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            setSelection(getText().length());
        }
    }
}