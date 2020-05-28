package org.pacman;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;

import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;

public class Map {

    /*
    PLEASE NOTE THAT HAVING A TOP FRAME FOR STATS THROWS OFF A LOT OF THINGS WHERE THE Y COORDINATE SHOULD BE ZERO
    AS SUCH, YOU WILL OFTEN SEE OFFSET_ZERO (equal to cell size) AS THE STARTING POINT SINCE THAT IS HOW BIG THE TOP FRAME IS.
     */

    //map constants (adjust for different pixels)

    //Map Configurations
    private double cellSize;
//    private final double CELL_SIZE = 18;
    private final String BACKGROUND_COLOR = "-fx-background-color: black";
    private final Color MAP_COLOR = Color.BLUEVIOLET;

    private static MapCell ghostStartPosition;
    private static MapCell pacmanStartPosition;

    //variables for window settings
    private final double[] FRAME_DIMENSIONS = new double[2];
    private final double[] MAP_DIMENSIONS = new double[2];

    //objects to attach to the main pain
    private final LinkedList<Node> mapObjects = new LinkedList<>();

    //variables for the current map, player, and statistics and game sound.
    private LinkedList<MapCell> currentMap; //list of cells that are drawn to the map.
    private LinkedList<MapCell> nonBorderCells; //used to randomly position player

    private Pane mapPane;
    private Scene mainScene;

    private Statistics mapStatistics;

    //====================================CONSTRUCTOR=================================================
    public Map(String map, double pixelSize) {
        this.cellSize = pixelSize;
        initGameLevel();
        initReadMap(map);
        initPane();
        initCellLinks();
        initDrawMap();
        initStatistics();
        initScene();
        initAttachMapObjects();
    }
    //================================================================================================


    //===================================INITIALIZING METHODS=========================================
    private void initGameLevel(){
        currentMap = new LinkedList<>();
        nonBorderCells = new LinkedList<>();
    }

    private void initReadMap(String map) {//map here will be used eventually once levels are implemented.
        InputStream inputStream = getClass().getResourceAsStream("/orig.txt");
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

        try(BufferedReader reader = new BufferedReader(inputStreamReader)){
            String input = "";
            int rowCount = 2; //start at 1 to allow for status bar.
            int columnCount = 0;
            char[] mapElements = null;
            while ((input = reader.readLine()) != null) {
                mapElements = input.toCharArray();
                for (char c : mapElements) {
                    MapCell cell;
                    switch(c){
                        case '#':
                            cell = new MapCell(columnCount * cellSize, rowCount * cellSize, cellSize, true, false, MAP_COLOR);
                            break;
                        case 'P':
                            cell = new MapCell(columnCount * cellSize, rowCount * cellSize, cellSize, false, true, null);
                            break;
                        default:
                            cell = new MapCell(columnCount * cellSize, rowCount * cellSize, cellSize, false, false, null);
                            if(c == 'G'){
                                cell.setGhostStart();
                                ghostStartPosition = cell;
                            }
                            if(c == 'U'){
                                cell.setPacmanStart();
                                pacmanStartPosition = cell;
                            }
                            if(c == 'B'){
                                cell.setBoosterCell();
                            }
                    }
                    currentMap.add(cell); //FIXME, once here, we will then loop through all cells and draw the map in another function.
                    if (!cell.isBorder() && !cell.isPortal()) {
                        nonBorderCells.add(cell);
                    }
                    columnCount++;
                }
                columnCount = 0;
                rowCount++;
            }
            FRAME_DIMENSIONS[0] = (mapElements != null) ? mapElements.length * cellSize : 0; //sets the width of the map.
            FRAME_DIMENSIONS[1] = rowCount * cellSize;
            MAP_DIMENSIONS[0] = FRAME_DIMENSIONS[0] - cellSize;
            MAP_DIMENSIONS[1] = FRAME_DIMENSIONS[1] - cellSize;
        }catch(IOException e){
            e.printStackTrace();
        }
    }//FIXME

    private void initPane() {
        mapPane = new Pane();
        mapPane.setPrefSize(FRAME_DIMENSIONS[0], FRAME_DIMENSIONS[1]);
        mapPane.setStyle(BACKGROUND_COLOR);
    }

    private void initCellLinks() {
        for (MapCell cell : currentMap) {
            cell.setCellNeighbors(MAP_DIMENSIONS, currentMap);
        }

    }

    private void initDrawMap() {
        for (MapCell cell : currentMap) {
            if (cell.isBorder()) {
                if (!cell.getBorders().isEmpty()) {
                    mapPane.getChildren().addAll(cell.getBorders());
                }
            } else {
                mapObjects.add(cell.getCellFood());
                MapCell.increaseMapFood();
            }
        }
    }

    private void initScene(){
        mainScene = new Scene(mapPane);
    }

    public void initPlayer(Pacman pacman) {
        mapPane.getChildren().add(pacman);
    }

    public void initGhost(Ghost ghost){
        mapPane.getChildren().addAll(ghost.getBody());

    }

    private void initStatistics() {
        mapStatistics = new Map.Statistics();
        mapObjects.add(mapStatistics.getSCORE_LABEL());
        mapObjects.add(mapStatistics.getTIME_LABEL());
    }

    private void initAttachMapObjects(){
        mapPane.getChildren().addAll(mapObjects);
    }
    //================================================================================================

    //=================================GETTERS========================================================
    public Scene getScene(){
        return mainScene;
    }

    public Pane getPane(){
        return mapPane;
    }

    public static MapCell getPacManStartingPosition() {//FIXME, needs to be based on the map indicator.
        return pacmanStartPosition;
    }

    public static MapCell getGhostStartingPosition(){//FIXME, needs to be based on the map indicator.
        return ghostStartPosition;
    }

    public int getMapFoodLeft(){
        return MapCell.getMapFoodLeft();
    }

    public String getTime(){
        return mapStatistics.getTime();
    }
    //================================================================================================

    //=================================SETTERS========================================================
    public void updateTime(){
        this.mapStatistics.updateTime();
    }

    public void updateScore(int score){
        this.mapStatistics.updateScore(score);
    }
    //================================================================================================

    //================================INNER CLASSES=======================================================
    private class Statistics {

        private final Label SCORE_LABEL;
        private final Label TIME_LABEL;
        private final double FONT_SIZE;
        private final Font font;

        private int score;
        private int secondFractions = 0;
        private int seconds = 0;
        private int minutes = 0;

        public Statistics(){
            this.SCORE_LABEL = new Label();
            this.TIME_LABEL = new Label();
            this.FONT_SIZE = FRAME_DIMENSIONS[0] / 30;
            font = new Font("Rainy Days", FONT_SIZE);
            setScorePosition(FRAME_DIMENSIONS[0] / 8, 0);
            setTimePosition(FRAME_DIMENSIONS[0] * 6 / 11, 0);
        }

        public void setScorePosition(double x, double y){
            this.SCORE_LABEL.setLayoutX(x);
            this.SCORE_LABEL.setLayoutY(y);
            this.SCORE_LABEL.setText("SCORE: " + score); //FIXME static variable.
            this.SCORE_LABEL.setFont(font);
            this.SCORE_LABEL.setTextFill(MAP_COLOR);
        }

        public void setTimePosition(double x, double y){
            this.TIME_LABEL.setLayoutX(x);
            this.TIME_LABEL.setLayoutY(y);
            this.TIME_LABEL.setText("TIME: "); //FIXME static variable.
            this.TIME_LABEL.setFont(font);
            this.TIME_LABEL.setTextFill(MAP_COLOR);
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

        public Label getSCORE_LABEL(){
            return SCORE_LABEL;
        }

        public Label getTIME_LABEL(){
            return TIME_LABEL;
        }

    }
}
