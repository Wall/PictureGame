package com.bluewall.picturegame.model;

import android.graphics.Bitmap;
import android.media.Image;

/**
 * Created by glipscombe on 12/01/2015.
 */
public class Question {

    private Bitmap image;
    private String question;
    private String answer;

    public Question(Bitmap image, String question, String answer){
        this.image = image;
        this.question = question;
        this.answer = answer;
    }

    public boolean isCorrectAnswer(String selectedAnswer){
        // Check if the entered answer is correct
        return true;
    }

    public String getQuestion(){
        return question;
    }

    public Bitmap getImage(){
        return image;
    }


}
