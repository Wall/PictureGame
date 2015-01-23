package com.bluewall.picturegame.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

/**
 * Created by dlee on 22/01/2015.
 */
public class InputText extends EditText {

    public static final String TAG = "InputText";

    /* List of all characters allowed in this inputText */
    private static final String VALID_CHARACTERS =
            "abcdefghijklmnopqrstuvwxyz" +
            "ABCDEFGHIJKLMNOPQRSTUVQXYZ" +
            "0123456789" +
            ",.<>?:;+=-" +
            "!@#$%^&*()`~";

    private Context context;

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
        this.context = context;
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
                setSelection(s.length());
                if (!s.toString().equals(visibleAnswer)) {
                    int cursorPosition = getSelectionStart();
                    if (cursorPosition > 0) {
                        if (s.length() < preLength) {
                            if (currentAnswer.length() > 0) {
                                currentAnswer.setLength(currentAnswer.length() - 1);
                            }
                        } else {
                            if (currentAnswer.length() < numCharacters) {
                                char c = s.charAt(s.length() - 1);
                                if (isValidChar(c)) {
                                    currentAnswer.append(c);
                                }
                            }
                        }
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
        this.answer = answer.trim().toLowerCase();
        buildVisibleAnswer();
        this.setText(visibleAnswer);

        int numSpaces = 0;
        for (int i = 0; i < answer.length(); ++i) {
            if (answer.charAt(i) == ' ') {
                ++numSpaces;
            }
        }
        numCharacters = answer.length() - numSpaces;
    }

    /*
     * Builds the visible answer to be displayed
     */
    private void buildVisibleAnswer() {
        StringBuilder visible = new StringBuilder();

        int count = 0;
        for (int i = 0; i < answer.length(); ++i) {
            if (answer.charAt(i) == ' ') {
                visible.append("  ");
            } else {
                if (count < currentAnswer.length()) {
                    visible.append(" " + currentAnswer.charAt(count) + " ");
                } else {
                    visible.append(" _ ");
                }
                ++count;
            }
        }
        visibleAnswer = visible.toString();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);
        setSelection(getText().length());
        return ret;
    }
}