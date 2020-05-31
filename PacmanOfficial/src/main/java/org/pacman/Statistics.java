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

    private static int level;
    private static int score;
    private static int secondFractions = 0;
    private static int seconds = 0;
    private static int minutes = 0;
    private Color color;

    public Statistics(){
        this.SCORE_LABEL = new Label();
        this.TIME_LABEL = new Label();
        this.LEVEL_LABEL = new Label();
    }

    public void configure(double [] frameDimensions, String fontName, Color color){
        this.color = color;
        this.fontSize = frameDimensions[0] / 30;
        font = new Font(fontName, fontSize);
    }

    public void updateScore(int score){
        this.score = score;
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

    public String getTime(){
        return String.format("%02d:%02d.%02d", minutes, seconds, secondFractions);
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
        level = 0;
    }

    public void setLevelPosition(double x, double y){
        configureLabel(this.LEVEL_LABEL, x, y, "LEVEL" + level);
    }

    public void setScorePosition(double x, double y){
        configureLabel(this.SCORE_LABEL, x, y, "SCORE" + score);
    }

    public void setTimePosition(double x, double y){
        configureLabel(this.TIME_LABEL, x, y, "TIME ");
    }

    private void configureLabel(Label label, double x, double y, String text){
        label.setText(text);
        label.setLayoutX(x);
        label.setLayoutY(y);
        label.setTextFill(color);
        label.setFont(font);
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

}

