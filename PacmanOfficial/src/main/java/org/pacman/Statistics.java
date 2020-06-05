package org.pacman;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;

import java.util.LinkedList;

public class Statistics {
    private final Label SCORE_LABEL;
    private final Label TIME_LABEL;
    private final Label LEVEL_LABEL;
    private double fontSize;
    private Font font;

    private static int level = 1;
    private static int score = 0;
    private static int lives = 3;
    private static int secondFractions = 0;
    private static int seconds = 0;
    private static int minutes = 0;

    private static LinkedList<Arc> lifeObjects = new LinkedList<>();
    private static Arc lifeOne = new Arc();
    private static Arc lifeTwo = new Arc();
    private static Arc lifeThree = new Arc();

    public Statistics(){
        this.SCORE_LABEL = new Label();
        this.TIME_LABEL = new Label();
        this.LEVEL_LABEL = new Label();
        this.LEVEL_LABEL.setText("LEVEL: " + level);
        initiateLives();
    }

    private void initiateLives(){
        lifeObjects.add(lifeOne);
        lifeObjects.add(lifeTwo);
        lifeObjects.add(lifeThree);
        for(Arc arc: lifeObjects){
            arc.setFill(Color.YELLOW);
            arc.setType(ArcType.ROUND);
            arc.setStartAngle(45);
            arc.setLength(270);
        }
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
        for(Arc arc: lifeObjects){
            arc.setRadiusY(frameDimensions[0] / 60);
            arc.setRadiusX(frameDimensions[0] / 60);
        }
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
        TIME_LABEL.setText(String.format("%02d : %02d . %02d", minutes, seconds, secondFractions));
    }

    public void updateLevel(){
        level++;
        this.LEVEL_LABEL.setText("LEVEL: " + level);
    }

    public void updateLives(){
        switch(lives){
            case 0:
                lifeOne.setFill(Color.BLACK);
                lifeTwo.setFill(Color.BLACK);
                lifeThree.setFill(Color.BLACK);
                break;
            case 1:
                lifeOne.setFill(Color.YELLOW);
                lifeTwo.setFill(Color.BLACK);
                lifeThree.setFill(Color.BLACK);
                break;
            case 2:
                lifeOne.setFill(Color.YELLOW);
                lifeTwo.setFill(Color.YELLOW);
                lifeThree.setFill(Color.BLACK);
                break;
            case 3:
                lifeOne.setFill(Color.YELLOW);
                lifeTwo.setFill(Color.YELLOW);
                lifeThree.setFill(Color.YELLOW);
                break;
        }
    }

    public void increaseLives(){
        if (!(lives == 3)){
            lives++;
        }
        updateLives();
    }

    public void decreaseLives(){
        if(lives != 0){
            lives --;
        }
        updateLives();
    }

    public static void increaseScore(int amount){
        score += amount;
    }

    public String getTime(){
        return String.format("%02d:%02d.%02d", minutes, seconds, secondFractions);
    }

    public int getScore(){
        return score;
    }

    public int getLevel(){
        return level;
    }

    public int getLives(){
        return lives;
    }



    public void reset(){
        score = 0;
        seconds = 0;
        secondFractions = 0;
        minutes = 0;
        level = 1;
        lives = 3;
        updateLives();
    }

    public Label getTIME_LABEL(){
        return TIME_LABEL;
    }

    public Label getSCORE_LABEL(){
            return SCORE_LABEL;
        }

    public Label getLEVEL_LABEL(){
        return LEVEL_LABEL;
    }

    public LinkedList<Arc> getLifeObjects(){
        return lifeObjects;
    }

}

