package org.pacman;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class Map {

    /*
    PLEASE NOTE THAT HAVING A TOP FRAME FOR STATS THROWS OFF A LOT OF THINGS WHERE THE Y COORDINATE SHOULD BE ZERO
    AS SUCH, YOU WILL OFTEN SEE OFFSET_ZERO (equal to cell size) AS THE STARTING POINT SINCE THAT IS HOW BIG THE TOP FRAME IS.
     */

    //map constants (adjust for different pixels)

    //Map Configurations
    private double cellSize;
    private final String BACKGROUND_COLOR = "-fx-background-color: black";
    private Color mapColor;

    private static List<MapCell> ghostStartPositions;
    private static MapCell pacmanStartPosition;
    private static int spawnCounter = 0;

    //variables for window settings
    private final double[] FRAME_DIMENSIONS = new double[2];
    private final double[] MAP_DIMENSIONS = new double[2];
    private static HBox statsBar;
    private static VBox leftColumn;
    private static VBox leftMiddleColumn;
    private static VBox rightMiddleColumn;
    private static HBox rightColumn;

    //objects to attach to the main pain
    private final LinkedList<Node> mapObjects = new LinkedList<>();

    //variables for the current map, player, and statistics and game sound.
    private static LinkedList<MapCell> currentMap; //list of cells that are drawn to the map.
    private static LinkedList<MapCell> nonBorderCells; //used to randomly position player

    private static Pane mapPane;
    private Scene mainScene;

    //====================================CONSTRUCTOR=================================================
    public Map(String map, double pixelSize, Color color) {
        this.cellSize = pixelSize;
        this.mapColor = color;
        initGameLevel();
        initReadMap(map);
        initPane();
        initStatisticsGrid();
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
        ghostStartPositions = new LinkedList<>();
    }

    private void initReadMap(String map) {//map here will be used eventually once levels are implemented.
        InputStream inputStream = getClass().getResourceAsStream("/" + map + ".txt");
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

        try(BufferedReader reader = new BufferedReader(inputStreamReader)){
            String input = "";
            int rowCount = 1; //start at 1 to allow for status bar.
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
                                ghostStartPositions.add(cell);
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
        statsBar = new HBox();
        leftColumn = new VBox();
        leftMiddleColumn = new VBox();
        rightMiddleColumn = new VBox();
        rightColumn = new HBox();
        statsBar.setPrefSize(mapPane.getPrefWidth(), cellSize);
        statsBar.setLayoutX(mapPane.getLayoutX() + cellSize / 2);
//        statsBar.setLayoutX(0);
        statsBar.setLayoutY(mapPane.getLayoutY());
        statsBar.setAlignment(Pos.CENTER);

        leftColumn.setPrefSize(statsBar.getPrefWidth() / 4, statsBar.getPrefHeight());
        leftMiddleColumn.setPrefSize(statsBar.getPrefWidth() / 4, statsBar.getPrefHeight());
        rightMiddleColumn.setPrefSize(statsBar.getPrefWidth() / 4, statsBar.getPrefHeight());
        rightColumn.setPrefSize(statsBar.getPrefWidth() / 4, statsBar.getPrefHeight());

        leftMiddleColumn.setAlignment(Pos.TOP_CENTER);
        rightMiddleColumn.setAlignment(Pos.TOP_CENTER);
        rightColumn.setAlignment(Pos.CENTER);
        rightColumn.setSpacing(rightColumn.getPrefWidth() / 9);

        statsBar.getChildren().add(leftColumn);
        statsBar.getChildren().add(leftMiddleColumn);
        statsBar.getChildren().add(rightMiddleColumn);
        statsBar.getChildren().add(rightColumn);

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
        statistics.configure(FRAME_DIMENSIONS, fontName, mapColor);
        leftColumn.getChildren().add(statistics.getTIME_LABEL());
        leftMiddleColumn.getChildren().add(statistics.getSCORE_LABEL());
        rightMiddleColumn.getChildren().add(statistics.getLEVEL_LABEL());
        rightColumn.getChildren().addAll(statistics.getLifeObjects());
    }

    private void initAttachMapObjects(){
        mapPane.getChildren().addAll(mapObjects);
    }

    public static void resetSpawnCounter(){
        spawnCounter = 0;
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
        MapCell temp = ghostStartPositions.get(spawnCounter);
        spawnCounter++;
        if(spawnCounter == ghostStartPositions.size()){
            spawnCounter = 0;
        }
        return temp;
    }

    public int getMapFoodLeft(){
        return MapCell.getMapFoodLeft();
    }
    //================================================================================================
}
