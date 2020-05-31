package org.pacman;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.*;
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
    private Color mapColor = Color.BLUEVIOLET;

    private static MapCell ghostStartPosition;
    private static MapCell pacmanStartPosition;

    //variables for window settings
    private final double[] FRAME_DIMENSIONS = new double[2];
    private final double[] MAP_DIMENSIONS = new double[2];
    private static HBox statsBar = new HBox();
    private static VBox leftColumn = new VBox();
    private static VBox middleColumn = new VBox();
    private static VBox rightColumn = new VBox();

    //objects to attach to the main pain
    private final LinkedList<Node> mapObjects = new LinkedList<>();

    //variables for the current map, player, and statistics and game sound.
    private static LinkedList<MapCell> currentMap; //list of cells that are drawn to the map.
    private static LinkedList<MapCell> nonBorderCells; //used to randomly position player

    private static Pane mapPane;
    private Scene mainScene;

    private Statistics mapStatistics;

    //====================================CONSTRUCTOR=================================================
    public Map(String map, double pixelSize, Color color) {
        this.cellSize = pixelSize;
        this.mapColor = color;
        initGameLevel();
        initReadMap(map);
        initPane();
//        initStatisticsGrid();
        initCellLinks();
        initDrawMap();
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
        InputStream inputStream = getClass().getResourceAsStream("/" + map + ".txt");
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
                            cell = new MapCell(columnCount * cellSize, rowCount * cellSize, cellSize, true, false, mapColor);
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
    }

    private void initPane() {
        mapPane = new Pane();
        mapPane.setPrefSize(FRAME_DIMENSIONS[0], FRAME_DIMENSIONS[1]);
        mapPane.setStyle(BACKGROUND_COLOR);
    }

    private void initStatisticsGrid(){
        statsBar.setPrefSize(mapPane.getPrefWidth(), cellSize * 2);
        statsBar.setLayoutX(0);
        statsBar.setLayoutY(0);
        leftColumn.setPrefSize(statsBar.getPrefWidth() / 3, statsBar.getPrefHeight());
        middleColumn.setPrefSize(statsBar.getPrefWidth() / 3, statsBar.getPrefHeight());
        rightColumn.setPrefSize(statsBar.getPrefWidth() / 3, statsBar.getPrefHeight());
        leftColumn.setAlignment(Pos.CENTER);
        middleColumn.setAlignment(Pos.CENTER);
        statsBar.getChildren().add(leftColumn);
        statsBar.getChildren().add(rightColumn);
        statsBar.getChildren().add(middleColumn);
        mapPane.getChildren().add(statsBar);
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

    public void attachStatistics(Statistics statistics, String fontName) {
        statistics.setLevelPosition(leftColumn.getLayoutX(), leftColumn.getPrefHeight() / 2);
        statistics.setScorePosition(middleColumn.getLayoutX(), middleColumn.getPrefHeight() / 2);
        statistics.setTimePosition(rightColumn.getLayoutX(), rightColumn.getPrefHeight() / 2);
        statistics.configure(FRAME_DIMENSIONS, fontName, mapColor);
        leftColumn.getChildren().add(statistics.getLEVEL_LABEL());
        middleColumn.getChildren().add(statistics.getSCORE_LABEL());
        rightColumn.getChildren().add(statistics.getTIME_LABEL());
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

    public double[] getFRAME_DIMENSIONS(){
        return FRAME_DIMENSIONS;
    }
    //================================================================================================
}
