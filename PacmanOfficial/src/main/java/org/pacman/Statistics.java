package org.pacman;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Statistics {
    private final Label SCORE_LABEL;
    private final Label TIME_LABEL;
    private final Label LEVEL_LABEL;
    private double fontSize;
    private Font font;

    private static int level = 1;
    private static int score = 0;
    private static int secondFractions = 0;
    private static int seconds = 0;
    private static int minutes = 0;

    public Statistics(){
        this.SCORE_LABEL = new Label();
        this.TIME_LABEL = new Label();
        this.LEVEL_LABEL = new Label();
        this.LEVEL_LABEL.setText("LEVEL: " + level);
    }

    public void configure(double [] frameDimensions, String fontName, Color color){
        this.fontSize = frameDimensions[0] / 30;
        font = new Font(fontName, fontSize);
        this.SCORE_LABEL.setTextFill(color);
        this.SCORE_LABEL.setFont(font);
        this.TIME_LABEL.setTextFill(color);
        this.TIME_LABEL.setFont(font);
        this.LEVEL_LABEL.setTextFill(color);
        this.LEVEL_LABEL.setFont(font);
    }

    public void updateScore(){
        SCORE_LABEL.setText("SCORE: " + score);
    }

    public void updateTime(){
        secondFractions += 1;
        if(secondFractions >= 60){
            secondFractions = 0;
            seconds++;
        }
        if(seconds >= 60){
            minutes++;
            seconds = 0;
        }
        TIME_LABEL.setText(String.format("TIME: %02d:%02d.%02d", minutes, seconds, secondFractions));
    }

    public void updateLevel(){
        level++;
        this.LEVEL_LABEL.setText("LEVEL: " + level);
    }

    public String getTime(){
        return String.format("%02d:%02d.%02d", minutes, seconds, secondFractions);
    }

    public static void increaseScore(int amount){
        score += amount;
    }

    public int getScore(){
        return this.score;
    }

    public int getLevel(){
        return level;
    }

    public void reset(){
        score = 0;
        seconds = 0;
        secondFractions = 0;
        minutes = 0;
        level = 1;
    }

    public Label getSCORE_LABEL(){
            return SCORE_LABEL;
        }

    public Label getTIME_LABEL(){
        return TIME_LABEL;
    }

    public Label getLEVEL_LABEL(){
        return LEVEL_LABEL;
    }

    public void setPosition(Label label, double x, double y){
        label.setLayoutX(x);
        label.setLayoutY(y);
    }

}

