package com.bluewall.picturegame.model;

/**
 * Created by glipscombe on 12/01/2015.
 */
public class Question {

    private String imageLink;
    private String question;
    private String answer;
    private boolean isCurrent;

    public void setImage(String image) {
        this.imageLink = image;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }



    public Question(){

    }

    public Question(String image, String question, String answer){
        this.imageLink = image;
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

    public String getImage(){
        return imageLink;
    }


}
