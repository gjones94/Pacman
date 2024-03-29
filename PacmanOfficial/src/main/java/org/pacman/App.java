package org.pacman;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;


/**
 * JavaFX App
 */
public class App extends Application {

    //--Main window elements
    private static Stage mainStage;
    private static MainMenu mainMenu;

//    private static OptionsMenu optionsMenu;
    private static LeaderBoard leaderBoardMenu;
    private static ScoreSaver scoreSaver;
    private static GameOverMenu gameOverMenu;
    private static Map gameMap;
    private static Statistics statistics;
    private static Color levelColor;


    private String map; //identifies which map to use
    private final String backgroundColor = "-fx-background-color: black";
    private final String fontPath = "RainyDays.ttf";
    private final String fontName = "Rainy Days"; //must match name of it exactly within the file


//    private final static String fontPath = "RainyDays.ttf";
//    private final static String fontName = "Rainy Days"; //must match name of it exactly within the file

    private Rectangle2D screenDimensions;
    private double pixelSize;

    //--loop, last update time, and keystrokes.
    private AnimationTimer gameLoop;
    private LinkedList<KeyCode> keyStack;
    private LongProperty lastUpdateTime;


    //--game objects
    private Pacman player;
    private int numberOfGhosts = 4; //FIXME should also be dependent on the map level.
    private List<Ghost> ghosts;
    private double playerSpeed;
    private double ghostSpeed;
    private int mapLevel = 1; //beginning level.
    private boolean gameOver = false;
    private boolean win = false;
    private boolean playerIsDead = false;
    private boolean ghostVulnerable = false;
    private int vulnerableTimer = 480;
    private boolean timerWarning = false;
    private int pauseTimer = 0;
    private static Text ghostScore;
    private Ghost tempGhost;


    private static boolean paused = false;
    private static final Path path = Path.of("./scores.txt");

    //--audio/video
    private final static String mainMenuMusic = "intro";
    private final static String gameMusic = "gameMusic";
    private final static String loseMusic = "lose";
    private final static String winMusic = "win";
    private final static String intenseMusic = "intense";

    private Music music;
    private SoundEffects soundEffects;


    /*DO NOT EVER PUT ANYTHING IN STATIC ESPECIALLY IF IT CONTAINS JAVAFX OBJECTS THAT INITIALIZE
    I HAD THIS HERE AND IT CAUSED ISSUES STARTING (ONLY WITH THE COMPILED JAR) SINCE THE TOOLKIT WAS NOT YET INITIALIZED TO CREATE JAVAFX OBJECTS
    */

    @Override
    public void start(Stage primaryStage){
        mainStage = primaryStage; //assign stage to variable to allow switching of scenes
        screenDimensions = Screen.getPrimary().getVisualBounds();
        initFonts();
        initAudio();
        initStatistics();
        showMainMenu(true); //this should have the start game method within it.
    }

    //=======================================INITIAL SETTINGS======================================================
    private void initFonts(){
        Font.loadFont(getClass().getResource("/" + fontPath).toExternalForm(), 1);
    }

    private void initAudio(){
        soundEffects = new SoundEffects();
        Thread soundThread = new Thread(new SoundRunnable(soundEffects));
        soundThread.start();

        music = new Music();
        music.selectSong(mainMenuMusic, true);
        Thread musicThread = new Thread(new MusicRunnable(music));
        musicThread.start(); //thread enters music class loop to play music when notified.
    }

    private void initStatistics(){
        statistics = new Statistics();
    }
    //============================================================================================================


    //=======================================GAME LIFECYCLE=======================================================
    private void showMainMenu(boolean startMusic){
        resetAllLevels();
        mainMenu = new MainMenu();
        mainStage.setScene(mainMenu.getScene());
        changeScene(mainMenu.getScene(), mainMenu.getPane());
        mainStage.show();
        mainStage.setOnCloseRequest(closeWindow);
        if(startMusic){
            changeMusic(mainMenuMusic, true);
        }
    }

    private void showEnd(){
        gameLoop.stop();
        music.stop();
        if(win){
            winMenu winMenu = new winMenu();
            changeMusic(winMusic, true); //FIXME - add different music here.
            changeScene(winMenu.getScene(), winMenu.getPane());
        }else{
            gameOverMenu = new GameOverMenu(statistics.getTime()); //FIXME need to add score here, in addition to time.
            changeScene(gameOverMenu.getScene(), gameOverMenu.getPane());
        }
    }

    private void showOptionsMenu(){

    }

    private void showNextLevelMenu(){
        NextLevelMenu nextLevel = new NextLevelMenu();
        music.stop();
        changeScene(nextLevel.getScene(), nextLevel.getPane());
    }

    private void showLeaderBoard(){
        leaderBoardMenu = new LeaderBoard();
        changeScene(leaderBoardMenu.getScene(), leaderBoardMenu.getPane());
    }

    private void saveUserScore(){
        scoreSaver = new ScoreSaver(statistics.getScore(), statistics.getTime());
        changeScene(scoreSaver.getScene(), scoreSaver.getPane());
    }

    private void startGame(){
        getLevelSettings();
        initPixelSize();
        configureMap();
        changeScene(gameMap.getScene(), gameMap.getPane()); //configure stage settings for screen position
        initKeyCommands(gameMap.getScene()); //bind keystroke actions to the main scene window
        initPacMan(); //create the pac-man player.
        initGhosts();
        initGhostScore();
        changeMusic(gameMusic, true);
        lastUpdateTime = new SimpleLongProperty();
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                onUpdate(now);
            }
        };
        gameLoop.start();
    }

    //===================================GAME INITIALIZERS=========================================================
    private void getLevelSettings(){

        if(mapLevel == 4) {
            map = "mostDifficult";
            levelColor = Color.DARKTURQUOISE;
        }else if(mapLevel == 3){
            map = "difficult";
            levelColor = Color.RED;
        }else if(mapLevel == 2){
            map = "intermediate";
            levelColor = Color.GREEN;
        }else{
            levelColor = Color.BLUE;
            map = "easy";
        }
    }

    private void initPixelSize(){//FIXME need to adjust speed too!!!
        screenDimensions = Screen.getPrimary().getVisualBounds();
//        if(map.equals("difficult")) {
//            if (screenDimensions.getHeight() < 1080) {
//                pixelSize = 18;
//                playerSpeed = 2.25;
//                ghostSpeed = 1.125;
//            } else {
//                pixelSize = 30;
//                playerSpeed = 3.75;
//                ghostSpeed = 1.875;
//            }
//        }else {
            if (screenDimensions.getHeight() < 1080) {
                pixelSize = 36;
                playerSpeed = 3;
                ghostSpeed = 2.25;
            } else {
                pixelSize = 48;
                playerSpeed = 4;
                ghostSpeed = 3;
            }
//        }
    }

    private void configureMap(){
        gameMap = new Map(map, pixelSize, levelColor); //FIXME also pass in a color here for the map difficulty.
        gameMap.attachStatistics(statistics, fontName);
    }

    private void changeScene(Scene scene, Pane pane){
        mainStage.setScene(scene); //puts the main scene onto the stage
        mainStage.setX((screenDimensions.getWidth() - pane.getPrefWidth()) / 2);
        mainStage.setY((screenDimensions.getHeight() - pane.getPrefHeight()) / 2);
        mainStage.show();
    }

    private void initKeyCommands(Scene scene){
        keyStack = new LinkedList<>();
        scene.setOnKeyPressed(event -> {
            if(event.getCode().equals(KeyCode.SPACE)){ //PAUSE GAME KEY
                if(paused){
                    paused = false;
                    music.play();
                    resumeGame();
                }else{
                    paused = true;
                    music.stop();
                    pauseGame();
                }
            }else {//MAIN KEY STROKES
                if (!keyStack.contains(event.getCode())) {
                    if (keyStack.size() == 2) {
                        keyStack.set(0, event.getCode());
                    } else {
                        keyStack.push(event.getCode());
                    }
                }
            }
        });
    }

    private void initPacMan(){//FIXME feed pixel size to me
        player = new Pacman(Map.getPacManStartingPosition(), playerSpeed); //sets the pacman in top left corner
        gameMap.initPlayer(player);
    }

    private void initGhosts(){
        ghosts = new LinkedList<>();

        for(int i = 0; i < numberOfGhosts; i++){
            Ghost ghost = new Ghost(Map.getGhostStartingPosition(), player, ghostSpeed);
            ghosts.add(ghost);
            gameMap.initGhost(ghost);
        }
    }

    private void initGhostScore(){
        ghostScore = new Text("100");
        ghostScore.setFill(Color.BLUE);
        ghostScore.setFont(Font.font("Arial", FontWeight.BOLD, pixelSize / 1.5));
    }
    //============================================================================================================

    private void checkIfGameOver(){
        if(gameMap.getMapFoodLeft() == 0){
            win = true;
            gameOver = true;
        }else if(playerIsDead){
            if(statistics.getLives() == 0){
                win =  false;
                gameOver = true;
            }else{
                statistics.decreaseLives();
            }
            pauseTimer = 120;
        }
    }

    private void resetGameVariables(){
        gameOver = false;
        Map.resetSpawnCounter();
        playerIsDead = false;
        win = false;
        paused = false;
        vulnerableTimer = 480;
        ghostVulnerable = false;
    }

    private void resetPlayers(){
        for(Ghost ghost: ghosts){
            ghost.resetPosition();
        }
        gameMap.getPane().getChildren().addAll(tempGhost.getBody());
        player.resetPosition();
        player.restore();
        music.play();
    }

    private void increaseLevel(){
        mapLevel++;
        if(mapLevel == 5){
            mapLevel = 1;
        }
        resetGameVariables();
        Ghost.increaseTrackingDistance();
        statistics.updateLevel();
        statistics.increaseLives();
        endVulnerability();
    }

    private void resetAllLevels(){
        resetGameVariables();
        MapCell.resetFood();
        statistics.reset();
        Ghost.resetTrackingDistance();
        numberOfGhosts = 4;
        mapLevel = 1; //FIXME NEED TO MODIFY THIS BACK.
    }

    private void changeMusic(String name, boolean resetPosition){
        if(music.isPlaying()){
            music.stop();
        }
        music.selectSong(name, resetPosition);
        music.play();
    }

    private void pauseGame(){
        gameLoop.stop();
    }

    private void resumeGame(){
        gameLoop.start();
    }

    private void killAudio(){
        /*this will allow the threads to to attempt to loop again,
        but since the music and sound classes are in a kill state,
        they won't re-enter the continuous loop they are in.
        */
        soundEffects.kill();
        soundEffects.play();
        soundEffects.stop();
        music.kill();
        music.play();
        music.stop();
    }

    private void exit(){
        killAudio();
        Platform.exit();
    }
    //============================================================================================================

    //=======================================GAME UPDATE==========================================================
    public void onUpdate(long timeStamp){
        if(pauseTimer == 0){
            if(!gameOver){
                updatePlayer(timeStamp);
                updateGhosts();
                updateGameStats();
                lastUpdateTime.set(timeStamp);
                checkIfGameOver();
            }else{
                showEnd();
            }
        }else{
            pauseTimer --;
            if(pauseTimer == 0 && ghostVulnerable){
                gameMap.getPane().getChildren().remove(ghostScore);
                player.setVisible();
            }else if(pauseTimer != 0 && playerIsDead){
                player.die();
                gameMap.getPane().getChildren().removeAll(tempGhost.getBody());
            }else if(pauseTimer == 0 && !gameOver){
                resetGameVariables();
                resetPlayers();
            }else{
                tempGhost = null;
            }
        }
    }

    public void updatePlayer(double elapsedTime){
        checkForKeyEvent(elapsedTime - lastUpdateTime.get());
        if(player.isInvincible()) {
            ghostVulnerable = true;
            vulnerableTimer = 480; //sets this again to make sure that initVulnerability kicks off every time that a booster is eaten.
            player.resetInvicibility(); //resets so that he can eat another booster.
        }
    }

    public void updateGhosts(){
        if(ghostVulnerable){
            if(vulnerableTimer != 0){
                if(vulnerableTimer == 480) {
                    initVulnerability();
                }else{
                    timerWarning = vulnerableTimer < 61 & vulnerableTimer % 10 == 0; //set timer warning if true, otherwise it will be false.
                }
                vulnerableTimer--;
           }else{
                endVulnerability();
           }
        }

        for(Ghost ghost: ghosts){
            if(!ghost.isAlive()){
                if(!ghost.tryRespawn()){
                    continue; //move to next ghost if he didn't come back to life.
                }
            }
            ghost.move();
            if(timerWarning){
                ghost.showWarning();
            }
            if(ghost.isAlive() && ghost.collidedWithPlayer()){
                if(ghost.isVulnerable()){
                    ghostScore.setX(ghost.getX());
                    ghostScore.setY(ghost.getY());
                    if(!gameMap.getPane().getChildren().contains(ghostScore)){
                        gameMap.getPane().getChildren().add(ghostScore);
                    }
                    player.setInvisible();
                    ghost.kill();
                    Statistics.increaseScore(100);
                    soundEffects.selectSound("siren");
                    soundEffects.play();
                    pauseTimer = 30;
                }
                else{ //player loses a life.
                    keyStack.clear();
                    music.stop();
                    soundEffects.selectSound("lose");
                    soundEffects.play();
                    tempGhost = ghost;
                    playerIsDead = true;
                    break;
                }
            }
        }
    }

    private void updateGameStats(){
        statistics.updateScore();
        statistics.updateTime();
    }

    private void initVulnerability(){
        if(ghostVulnerable){
            changeMusic(intenseMusic, false);
        }else{
            changeMusic(intenseMusic, true);
        }

        for(Ghost ghost: ghosts){
            ghost.setVulnerable();
        }
    }

    private void endVulnerability(){
        ghostVulnerable = false;
        vulnerableTimer = 480;
        timerWarning = false;
        changeMusic(gameMusic, false);
        for(Ghost ghost: ghosts){
            ghost.setNotVulnerable();
        }
    }
    //============================================================================================================

    //======================================CLICK AND KEY EVENTS==================================================
    EventHandler<WindowEvent> closeWindow = windowEvent -> exit(); //kicks off exit method to kill audio threads.

    EventHandler<MouseEvent> mouseClicked = event -> {
        MenuButton button = (MenuButton) event.getSource();
        executeButton(button.getIdentifier());
    };

    EventHandler<KeyEvent> enterKeyPressed = keyEvent -> {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            MenuButton button = (MenuButton) keyEvent.getSource();
            executeButton(button.getIdentifier());
        }
    };

    private void executeButton(String identifier){
        switch (identifier){
            case "START":
                startGame();
                break;
            case "PLAY AGAIN":
                resetAllLevels();
                startGame();
                break;
            case "NEXT LEVEL":
                showNextLevelMenu();
                break;
            case "START LEVEL !":
                increaseLevel();
                startGame();
                break;
            case "MAIN MENU":
                showMainMenu(false);
                break;
            case "LEADER BOARD":
            case "CANCEL":
                showLeaderBoard();
                break;
            case "OPTIONS":
                break;
            case "SAVE SCORE":
                saveUserScore();
                break;
            case "SAVE":
                scoreSaver.addScoreToList();
                changeMusic(mainMenuMusic, true);
                showLeaderBoard();
                break;
            case "EXIT":
                exit();
                break;

        }
    }

    private void checkForKeyEvent(double elapsedTime){ //obtains keystroke from user and adds to a keyCode Stack to execute.
        boolean currentKeyCanMove;
        boolean nextKeyCanMove = false;

        if(!keyStack.isEmpty() && elapsedTime > 0.0){
            currentKeyCanMove = executeKey(keyStack.peekLast());

            if(keyStack.peekFirst() != keyStack.peekLast()){
                nextKeyCanMove = executeKey(keyStack.peekFirst());
            }
            if(!currentKeyCanMove || nextKeyCanMove){
                keyStack.removeLast();
            }
        }
    }

    private boolean executeKey(KeyCode key){
        switch (key) {
            case UP:
            case W:
            case Y:
                return player.moveUp();
            case DOWN:
            case S:
            case H:
                return player.moveDown();
            case RIGHT:
            case D:
            case J:
                return player.moveRight();
            case LEFT:
            case A:
            case G:
                return player.moveLeft();
            default:
                return false;
        }
    }
    //============================================================================================================

    //=====================================INNER CLASSES==========================================================
    private class MainMenu {

        private final Pane pane = new Pane();
        private final HBox topHBox = new HBox();
        private final VBox vbox = new VBox();
        private final Scene startMenuScene;
        private final List<Circle> borderDots = new LinkedList<>();
        private final List<MenuButton> buttons = new LinkedList<>();

        public MainMenu(){
            initGrid();
            initDrawStartMenuGraphics();
            initButtons();
            initButtonClickEvents();
            initDrawLogo();
            startMenuScene = new Scene(pane);
            startMenuScene.getStylesheets().addAll("/styles.css");
        }

        private void initGrid(){
            pane.setPrefSize(screenDimensions.getWidth() * .8, screenDimensions.getHeight() *.8);
            pane.setStyle(backgroundColor);

            //pac man logo area
            topHBox.setPrefSize(pane.getPrefWidth() * .90, pane.getPrefHeight() * .3);
            topHBox.setLayoutY(pane.getPrefHeight() * .06);
            topHBox.setLayoutX((pane.getPrefWidth() - topHBox.getPrefWidth()) / 2);
            topHBox.setAlignment(Pos.CENTER);

            //BUTTON OPTION BOX
            vbox.setAlignment(Pos.TOP_CENTER);
            vbox.setPrefHeight(pane.getPrefHeight() * 1 / 4);
            vbox.setPrefWidth(pane.getPrefWidth() * 1 / 2);
            vbox.setLayoutX((pane.getPrefWidth() - vbox.getPrefWidth()) / 2);
            vbox.setLayoutY(topHBox.getLayoutY() + topHBox.getPrefHeight());
            pane.getChildren().addAll(vbox, topHBox);
        }

        private void initDrawStartMenuGraphics(){

            double paneHeight = pane.getPrefHeight();
            double paneWidth = pane.getPrefWidth();

            double spacing = 20;
            while(paneHeight % spacing != 0 && paneWidth % spacing != 0){
                spacing +=  1;
            }

            for(double i = spacing; i < pane.getPrefWidth(); i+= spacing){
                Circle dot = new Circle(i, spacing, spacing / 10, Color.YELLOW);
                borderDots.add(dot);
            }

            for(double i = spacing; i < pane.getPrefHeight() - spacing; i+= spacing){
                Circle dot = new Circle(spacing, i, spacing / 10, Color.YELLOW);
                borderDots.add(dot);
            }

            for(double i = spacing; i < pane.getPrefWidth(); i += spacing){
                Circle dot = new Circle(i,pane.getPrefHeight() - spacing, spacing / 10, Color.YELLOW);
                borderDots.add(dot);
            }

            for(double i = spacing; i < pane.getPrefHeight() - spacing; i+= spacing){
                Circle dot = new Circle(pane.getPrefWidth() - spacing, i, spacing / 10, Color.YELLOW);
                borderDots.add(dot);
            }


            pane.getChildren().addAll(borderDots);
        }

        private void initButtons() {
            Font font = new Font(fontName, vbox.getPrefWidth() / 16);
            double width = vbox.getPrefWidth();
            double height = vbox.getPrefHeight() / 10;

            MenuButton start = new MenuButton("START", font, width, height);
            MenuButton leaderBoard = new MenuButton("LEADER BOARD", font, width, height);
            MenuButton options = new MenuButton("OPTIONS", font, width, height);
            MenuButton exit = new MenuButton("EXIT", font, width, height);

            buttons.add(start);
            buttons.add(leaderBoard);
            buttons.add(options);
            buttons.add(exit);

            vbox.getChildren().addAll(buttons);
        }

        private void initButtonClickEvents(){
            for(MenuButton btn: buttons){
                btn.setOnMouseClicked(mouseClicked);
                btn.setOnKeyPressed(enterKeyPressed);
            }
        }

        private void initDrawLogo() {
            Text mainMenu = new Text("P A C M A N");
            mainMenu.setFont(new Font(fontName, topHBox.getPrefHeight() / 1.5));
            mainMenu.setFill(Color.YELLOW);
            topHBox.getChildren().add(mainMenu);
        }

        public Scene getScene(){
            return startMenuScene;
        }

        public Pane getPane(){
            return this.pane;
        }

    }

    private class winMenu {
        private final Pane pane = new Pane();
        private final HBox upperHBox = new HBox();
        private final HBox lowerHBox = new HBox();
        private final VBox upperVBox  = new VBox();
        private final Scene winScene;
        private Font font;
        private MenuButton startButton;

        public winMenu(){
            initGrid();
            initLabels();
            initButton();
            winScene = new Scene(pane);
            winScene.getStylesheets().addAll("/styles.css");
        }

        private void initGrid(){
            pane.setPrefSize(screenDimensions.getWidth() * .8, screenDimensions.getHeight() *.8);
            pane.setStyle(backgroundColor);

            upperHBox.setAlignment(Pos.CENTER);
            upperHBox.setPrefSize(pane.getPrefWidth(), pane.getPrefHeight() / 2);
            lowerHBox.setAlignment(Pos.CENTER);
            lowerHBox.setPrefSize(pane.getPrefWidth(), pane.getPrefHeight() / 2);
            lowerHBox.setLayoutY(upperHBox.getLayoutY() + upperHBox.getPrefHeight());
            upperHBox.getChildren().add(upperVBox);

            upperVBox.setPrefSize(upperHBox.getPrefWidth() / 1.5, upperHBox.getPrefHeight());

            pane.getChildren().add(lowerHBox);
            pane.getChildren().add(upperHBox);

            font = new Font(fontName, lowerHBox.getPrefWidth() / 16);
        }

        private void initLabels(){
            Label level = new Label();
            Label time = new Label();
            Label score = new Label();
            level.setText("LEVEL: " + statistics.getLevel());
            time.setText("TIME: " + statistics.getTime());
            score.setText("SCORE: " + statistics.getScore());
            level.setFont(font);
            time.setFont(font);
            score.setFont(font);
            upperVBox.getChildren().addAll(level, score, time);
        }

        private void initButton(){
            startButton = new MenuButton("NEXT LEVEL", font, upperHBox.getPrefWidth() * 3 / 5, lowerHBox.getPrefHeight() / 5);
            startButton.setOnKeyPressed(enterKeyPressed);
            startButton.setOnMouseClicked(mouseClicked);
            lowerHBox.getChildren().add(startButton);
        }

        public Scene getScene(){
            return this.winScene;
        }

        public Pane getPane(){
            return this.pane;
        }
    }

    private class NextLevelMenu {
        private Pane pane;
        private Scene scene;
        private MenuButton go;
        private Label level;

        public NextLevelMenu(){
            level = new Label("LEVEL " + (statistics.getLevel() + 1));
            pane = new Pane();

            pane.setPrefSize(screenDimensions.getWidth() * .8, screenDimensions.getHeight() * .8);
            pane.setStyle(backgroundColor);
            level.setFont(new Font(fontName, pane.getPrefWidth() / 15));

            VBox vBox = new VBox();
            vBox.setPrefSize(pane.getPrefWidth(), pane.getPrefHeight());
            vBox.setAlignment(Pos.CENTER);
            vBox.setSpacing(vBox.getPrefWidth() / 10);
            go = new MenuButton("START LEVEL !", new Font(fontName, vBox.getPrefWidth() / 20), pane.getPrefWidth() / 2, pane.getPrefHeight() / 9);
            vBox.getChildren().add(level);
            vBox.getChildren().add(go);
            pane.getChildren().add(vBox);
            scene = new Scene(pane);
            scene.getStylesheets().add("styles.css");
            go.setOnMouseClicked(mouseClicked);
            go.setOnKeyPressed(enterKeyPressed);
        }

        public Pane getPane(){
            return pane;
        }

        public Scene getScene(){
            return scene;
        }
    }

    private class GameOverMenu {

        private final Pane pane = new Pane();

        private final HBox upperHBox = new HBox();
        private final HBox middleHBox = new HBox();
        private final HBox lowerHBox = new HBox();
        private final VBox upperVBox = new VBox();
        private final VBox lowerVBox = new VBox();

        private Scene endScene;

        private MenuButton returnToMain;
        private MenuButton saveScore;
        private MenuButton playAgain;
        private MenuButton exit;
        private List<MenuButton> buttons = new LinkedList<>();

        public GameOverMenu(String time){
            initGrid();
            initLogo();
            initEndGameText();
            initButtons();
            initButtonClickEvents();
            initScene();
        }

        private void initGrid(){
            pane.setPrefSize(screenDimensions.getWidth() / 2, screenDimensions.getHeight() * .60);
            pane.setStyle(backgroundColor);
            upperVBox.setAlignment(Pos.TOP_CENTER);
            upperHBox.setPrefWidth(pane.getPrefWidth());
            upperHBox.setPrefHeight(pane.getPrefHeight() * .45);
            upperHBox.setStyle(backgroundColor);
            upperHBox.setAlignment(Pos.TOP_CENTER);
            middleHBox.setPrefSize(pane.getPrefWidth(), pane.getPrefHeight() * .26);
            middleHBox.setAlignment(Pos.CENTER);
            middleHBox.setLayoutY(upperHBox.getLayoutY() + upperHBox.getPrefHeight());
            middleHBox.setStyle(backgroundColor);
            lowerHBox.setPrefWidth(pane.getPrefWidth());
            lowerHBox.setPrefHeight(pane.getPrefHeight() * .29);
            lowerHBox.setStyle(backgroundColor);
            lowerHBox.setLayoutY(middleHBox.getLayoutY() + middleHBox.getPrefHeight());
            lowerHBox.getChildren().addAll(lowerVBox);
            lowerHBox.setAlignment(Pos.CENTER);
            lowerHBox.setSpacing(lowerHBox.getPrefWidth() / 20);
            upperHBox.getChildren().addAll(upperVBox);
            pane.getChildren().addAll(upperHBox, lowerHBox, middleHBox);
        }

        private void initLogo(){
            double arcCenterX = middleHBox.getPrefWidth() / 2;
            double arcCenterY = middleHBox.getLayoutY() + (.5 * middleHBox.getPrefHeight());
            double arcStartAngle = 315;
            double arcLength = -270;
            double arcRadius = upperHBox.getPrefHeight() * 10 / 27;
            double eyeCenterX = arcCenterX;
            double eyeCenterY = arcCenterY - arcRadius / 2;
            double eyeRadius = arcRadius / 4;
            Arc pacMan = new Arc(arcCenterX, arcCenterY, arcRadius, arcRadius, arcStartAngle, arcLength);
            pacMan.setType(ArcType.ROUND);
            pacMan.setFill(Color.YELLOW);
            Circle eyeBallShade = new Circle(eyeCenterX, eyeCenterY, eyeRadius + 2, Color.BLACK);
            Circle eyeBall = new Circle(eyeCenterX, eyeCenterY, eyeRadius, Color.WHITE);
            Circle pupil = new Circle(eyeCenterX + (eyeBall.getRadius() / 2), eyeCenterY - (eyeBall.getRadius() / 2), eyeRadius / 3, Color.BLUE);
            pane.getChildren().addAll(pacMan, eyeBallShade, eyeBall, pupil);
        }

        private void initEndGameText(){
            double fontSize = pane.getPrefHeight() / 9;
            int oldHighScore;

            Text score = new Text("SCORE - " + statistics.getScore());
            Font font = new Font(fontName, fontSize);
            Color color = Color.YELLOW;


            score.setFill(color);
            score.setFont(font);


            Text officialTime = new Text("TIME - " + statistics.getTime());
            officialTime.setFont(font);
            officialTime.setFill(color);

            //see if player's score is the new high score.
            oldHighScore = readHighestScore();
            String outcome = (statistics.getScore() > oldHighScore) ? "NEW HIGH SCORE!" : "GAME OVER";
            Text gameOver = new Text(outcome);


            gameOver.setFont(font);
            gameOver.setFill(color);

            upperVBox.getChildren().addAll(gameOver, score, officialTime); //NOTE, THIS ALSO DETERMINES THE ORDER OF THE BUTTON ARRANGEMENT
        }

        private int readHighestScore(){
                //read from external file
                try(FileInputStream fis = new FileInputStream(path.toString())){
                    BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                    String readData;
                    readData = reader.readLine();
                    String[] strArray = readData.split(",");
                    return Integer.parseInt(strArray[1]);

                }catch(IOException e){

                }

                return 0;
        }

        private void initButtons(){
            Font font = new Font(fontName, lowerHBox.getPrefHeight() / 12);
            double width = lowerHBox.getPrefWidth() / 6;
            double height = lowerHBox.getPrefHeight() / 2;

            saveScore = new MenuButton("SAVE SCORE", font, width, height);
            returnToMain = new MenuButton("MAIN MENU", font, width, height);
            playAgain = new MenuButton("PLAY AGAIN", font, width, height);
            exit = new MenuButton("EXIT", font, width, height);

            buttons.add(saveScore);
            buttons.add(playAgain);
            buttons.add(returnToMain);
            buttons.add(exit); //NOTE, THIS ALSO DETERMINES THE ORDER OF THE BUTTON ARRANGEMENT

            lowerHBox.getChildren().addAll(buttons);
        }

        private void initButtonClickEvents(){
            for(MenuButton button: buttons){
                button.setOnKeyPressed(enterKeyPressed);
                button.setOnMouseClicked(mouseClicked);
            }
        }

        private void initScene(){
            endScene = new Scene(pane);
            endScene.getStylesheets().addAll("styles.css");
        }

        public Scene getScene(){
            return endScene;
        }

        public Pane getPane(){
            return this.pane;
        }
    }

    private class ScoreSaver{
        private final List<Score> scores = new LinkedList<>();
        private Pane pane;
        private Scene scene;
        private HBox upperHBox = new HBox();
        private HBox middleHBox = new HBox();
        private HBox lowerHBox = new HBox();
        private HBox buttonHBox = new HBox();
        private Font font;

        private VBox middleLeftVbox = new VBox();
        private VBox middleMidVbox = new VBox();
        private VBox middleRightVbox = new VBox();

        private VBox lowerLeftVbox = new VBox();
        private VBox lowerMiddleVbox = new VBox();
        private VBox lowerRightVbox = new VBox();

        private final List<Text> headers = new LinkedList<>();
        private final List<VBox> vBoxes = new LinkedList<>();
        private final List<HBox> hBoxes = new LinkedList<>();
        private final List<MenuButton> buttons = new LinkedList<>();

        private MenuButton saveButton;
        private MenuButton cancelButton;

        private int score;
        private String time;
        private TextField inputName;

        private Score newScore;


        public ScoreSaver(int score, String time){
            this.score = score;
            this.time = time;
            initPane();
            initScene();
            initGrid();
            initGridStyles();
            initHeaders();
            initTextInput();;
            initButtons();
            initButtonClickEvents();
        }

        public void initPane(){
            pane = new Pane();
            pane.setPrefSize(screenDimensions.getWidth() / 2, screenDimensions.getHeight() / 2);
            pane.setStyle(backgroundColor);
        }

        public void initScene(){
            scene = new Scene(pane);
            scene.getStylesheets().add("styles.css");
        }

        public void initGrid(){
            upperHBox.setPrefSize(pane.getPrefWidth(), pane.getPrefHeight() / 5);
            middleHBox.setPrefSize(pane.getPrefWidth(), pane.getPrefHeight() / 5);
            lowerHBox.setPrefSize(pane.getPrefWidth(), pane.getPrefHeight() / 5);
            buttonHBox.setPrefSize(pane.getPrefWidth(), pane.getPrefHeight() * 2 / 5);
            upperHBox.setLayoutY(0);
            middleHBox.setLayoutY(upperHBox.getPrefHeight());
            lowerHBox.setLayoutY(middleHBox.getLayoutY() + middleHBox.getPrefHeight());
            buttonHBox.setLayoutY(lowerHBox.getLayoutY() + lowerHBox.getPrefHeight());

            middleLeftVbox.setLayoutX(0);
            middleLeftVbox.setPrefSize(pane.getPrefWidth() / 3, middleHBox.getPrefHeight());
            lowerLeftVbox.setLayoutX(0);
            lowerLeftVbox.setPrefSize(pane.getPrefWidth() / 3, lowerHBox.getPrefHeight());

            middleMidVbox.setLayoutX(middleHBox.getPrefWidth() / 3);
            middleMidVbox.setPrefSize(pane.getPrefWidth() / 3, middleHBox.getPrefHeight());
            lowerMiddleVbox.setLayoutX(lowerHBox.getPrefWidth() / 3);
            lowerMiddleVbox.setPrefSize(pane.getPrefWidth() / 3, lowerHBox.getPrefHeight());

            middleRightVbox.setLayoutX(middleHBox.getPrefWidth() * 2 / 3);
            middleRightVbox.setPrefSize(pane.getPrefWidth() / 3, pane.getPrefHeight() / 3);
            lowerRightVbox.setLayoutX(middleHBox.getPrefWidth() * 2 / 3);
            lowerRightVbox.setPrefSize(pane.getPrefWidth() / 3, pane.getPrefHeight() / 3);

            middleHBox.getChildren().addAll(middleLeftVbox, middleMidVbox, middleRightVbox);
            lowerHBox.getChildren().addAll(lowerLeftVbox, lowerMiddleVbox, lowerRightVbox);

            hBoxes.add(lowerHBox);
            hBoxes.add(middleHBox);
            hBoxes.add(upperHBox);
            hBoxes.add(buttonHBox);

            vBoxes.add(lowerLeftVbox);
            vBoxes.add(lowerMiddleVbox);
            vBoxes.add(lowerRightVbox);
            vBoxes.add(middleLeftVbox);
            vBoxes.add(middleMidVbox);
            vBoxes.add(middleRightVbox);

            pane.getChildren().addAll(hBoxes);
        }

        private void initGridStyles(){
            for(HBox hBox: hBoxes){
                hBox.setAlignment(Pos.CENTER);
                hBox.setStyle(backgroundColor);
            }
            for(VBox vBox: vBoxes){
                vBox.setAlignment(Pos.CENTER);
                vBox.setStyle(backgroundColor);
            }
        }

        private void initHeaders(){
            font = new Font(fontName, upperHBox.getPrefHeight()  / 2);
            Text saveScoreHeader = new Text("S  A  V  E     S  C  O  R  E");
            saveScoreHeader.setFill(Color.YELLOW);
            saveScoreHeader.setFont(font);

            Text nameHeader = new Text("NAME");
            Text scoreHeader = new Text("SCORE");
            Text timeHeader = new Text("TIME");

            headers.add(timeHeader);
            headers.add(scoreHeader);
            headers.add(nameHeader);

            for(Text header: headers){
                header.setFill(Color.BLUE);
                header.setFont(font);
            }

            //add headers to grid
            upperHBox.getChildren().addAll(saveScoreHeader);
            middleLeftVbox.getChildren().add(nameHeader);
            middleMidVbox.getChildren().add(scoreHeader);
            middleRightVbox.getChildren().add(timeHeader);
        }

        private void initTextInput(){
            Text newScore = new Text(String.valueOf(this.score));
            Text newTime = new Text(time);
            newScore.setFont(font);
            newScore.setFill(Color.BLUEVIOLET);
            newTime.setFont(font);
            newTime.setFill(Color.BLUEVIOLET);


            inputName = new TextField();
            inputName.requestFocus();
            inputName.setId("TEXT_FIELD");
            inputName.setPrefSize(middleMidVbox.getPrefWidth() / 2, middleHBox.getPrefHeight() / 2);
            inputName.setFont(font);
            inputName.setAlignment(Pos.CENTER);

            lowerLeftVbox.getChildren().add(inputName);
            lowerMiddleVbox.getChildren().add(newScore);
            lowerRightVbox.getChildren().add(newTime);
        }

        private void initButtons(){
            double width = buttonHBox.getPrefWidth() * .4;
            double height = buttonHBox.getPrefHeight() * .4;
            saveButton = new MenuButton("SAVE", font, width, height);
            cancelButton = new MenuButton("CANCEL", font, width, height);
            buttons.add(saveButton);
            buttons.add(cancelButton);
            buttonHBox.getChildren().addAll(buttons);
            buttonHBox.setSpacing(buttonHBox.getPrefHeight() / 14);
            saveButton.requestFocus();
        }

        private void initButtonClickEvents(){
            for(MenuButton button: buttons){
                button.setOnKeyPressed(enterKeyPressed);
                button.setOnMouseClicked(mouseClicked);
            }
        }

        public void addScoreToList(){
            readScores();
            newScore = new Score(inputName.getText(), this.score, this.time);
            scores.add(newScore); //add new scores
            Collections.sort(scores);
            writeScores();
        }

        private void readScores(){
            //read from external file
            try(FileInputStream fis = new FileInputStream(path.toString())){
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                String readData;
                while((readData = reader.readLine()) != null){
                    String[] strArray = readData.split(",");
                    Score tempScore = new Score(strArray[0], Integer.parseInt(strArray[1]), strArray[2]);
                    scores.add(tempScore);
                }

            }catch(IOException e){

            }
        }

        private void writeScores(){
            try(BufferedWriter bw = Files.newBufferedWriter(path)){
                for(Score tempScore: scores){
                    String name = tempScore.getName();
                    String score = String.valueOf(tempScore.getScore());
                    String time = tempScore.getTime();
                    bw.write(name + "," + score + "," + time + "\n");
                }


            }catch(IOException e){

            }
        }

        public Scene getScene(){
            return scene;
        }

        public Pane getPane(){
            return pane;
        }

        private class Score implements Comparable<Score>{
            String name;
            String time;
            int score;


            public Score(String name, int score, String time){
                this.name = name;
                this.time = time;
                this.score = score;
            }

            private String getName(){
                return this.name;
            }


            private int getScore(){
                return this.score;
            }

            private String getTime(){
                return this.time;
            }

            @Override
            public int compareTo(Score o) {
                if(this.score == o.score){
                    return this.time.compareTo(o.time);
                }else if(this.score < o.score){
                    return 1;
                }else{
                    return -1;
                }
            }

        }
    }

    private class LeaderBoard{

        private Font font;
        private final LinkedList<String> scoreEntries = new LinkedList<>();
        private Pane pane;
        private Scene scene;
        private VBox rankingColumn;
        private VBox nameColumn;
        private VBox scoreColumn;
        private VBox timeColumn;
        private VBox leftButtonColumn;
        private VBox rightButtonColumn;
        private HBox upperHBox;
        private HBox middleHBox;
        private HBox lowerHBox;

        private MenuButton mainMenu;
        private MenuButton exit;
        private final List<MenuButton> buttons = new LinkedList<>();
        private final List<Text> headerItems = new LinkedList<>();

        public LeaderBoard(){
            initPane();
            initScene(); //loads the scene after all elements are on the pane.
            initGrid();
            loadScores();
            initButtons();
            initButtonClickEvents();
            initLeaderboard();
        }

        private void initPane(){
            pane = new Pane();
            pane.setPrefSize(screenDimensions.getWidth() / 2, screenDimensions.getWidth() / 2);
            pane.setStyle(backgroundColor);
        }

        private void initScene(){
            scene = new Scene(pane);
            scene.getStylesheets().add("styles.css");
        }

        private void initGrid(){

            //CREATE HBOXES (ROWS)
            upperHBox = new HBox();
            middleHBox = new HBox();
            lowerHBox = new HBox();

            upperHBox.setAlignment(Pos.CENTER);
            middleHBox.setAlignment(Pos.CENTER);
            lowerHBox.setAlignment(Pos.CENTER);
            lowerHBox.setSpacing(lowerHBox.getPrefWidth() / 2);

            upperHBox.setPrefSize(pane.getPrefWidth(), pane.getPrefHeight() * .2);
            middleHBox.setPrefSize(pane.getPrefWidth(), pane.getPrefHeight() * .6);
            lowerHBox.setPrefSize(pane.getPrefWidth(), pane.getPrefHeight() * .2);
            middleHBox.setLayoutY(upperHBox.getPrefHeight());
            lowerHBox.setLayoutY(middleHBox.getLayoutY() + middleHBox.getPrefHeight());

            //CREATE VBOXES (COLUMNS)
            double vBoxHeight = middleHBox.getPrefHeight();
            double vBoxWidth = pane.getPrefWidth() / 4;

            rankingColumn = new VBox();
            rankingColumn.setPrefSize(vBoxWidth, vBoxHeight);
            rankingColumn.setLayoutX(0);

            nameColumn = new VBox();
            nameColumn.setPrefSize(vBoxWidth, vBoxHeight);
            nameColumn.setLayoutX(vBoxWidth);

            scoreColumn = new VBox();
            scoreColumn.setPrefSize(vBoxWidth, vBoxHeight);
            scoreColumn.setLayoutX(2 * vBoxWidth);

            timeColumn = new VBox();
            timeColumn.setPrefSize(vBoxWidth, vBoxHeight);
            timeColumn.setLayoutX(3 * vBoxWidth);

            leftButtonColumn = new VBox();
            leftButtonColumn.setAlignment(Pos.CENTER);
            leftButtonColumn.setPrefSize(lowerHBox.getPrefWidth() / 2, lowerHBox.getPrefHeight());
            leftButtonColumn.setLayoutX(0);

            rightButtonColumn = new VBox();
            rightButtonColumn.setAlignment(Pos.CENTER);
            rightButtonColumn.setPrefSize(lowerHBox.getPrefWidth() / 2, lowerHBox.getPrefHeight());
            rightButtonColumn.setLayoutX(lowerHBox.getPrefWidth() / 2);

            font = new Font(fontName, nameColumn.getPrefWidth() / 10);
        }

        private void loadScores(){
            try(FileInputStream fis = new FileInputStream(path.toString())){
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                String input;
                while ((input = reader.readLine()) != null) {
                    scoreEntries.add(input);
                }
            }catch (IOException e){
                //no high scores to show.
            }
        }

        private void initButtons(){
            double width = rightButtonColumn.getPrefWidth() / 2;
            double height = rightButtonColumn.getPrefHeight() * .4;
            mainMenu = new MenuButton("MAIN MENU", font, width, height);
            exit = new MenuButton("EXIT", font, width, height);
            buttons.add(mainMenu);
            buttons.add(exit);
        }

        private void initButtonClickEvents(){
            for(MenuButton button: buttons){
                button.setOnMouseClicked(mouseClicked);
                button.setOnKeyPressed(enterKeyPressed);
            }
        }

        private void initLeaderboard(){

            //CREATE MAIN HEADER LOGO FOR LEADERBOARD
            Text leaderBoardLogo = new Text("L E A D E R   B O A R D");
            leaderBoardLogo.setFont(new Font(fontName, upperHBox.getPrefHeight() / 3));
            leaderBoardLogo.setFill(Color.YELLOW);

            //CREATE HEADERS
            Text rankHeader = new Text("RANK"), nameHeader = new Text("NAME"), scoreHeader = new Text("SCORE"), timeHeader = new Text("TIME");
            headerItems.add(rankHeader);
            headerItems.add(nameHeader);
            headerItems.add(scoreHeader);
            headerItems.add(timeHeader);

            for(Text header: headerItems){
                header.setId("HEADER_TEXT");
                header.setFont(font);
                header.setFill(Color.BLUE);
            }
            //VBOX ADD HEADERS
            rankingColumn.getChildren().add(rankHeader);
            nameColumn.getChildren().add(nameHeader);
            scoreColumn.getChildren().add(scoreHeader);
            timeColumn.getChildren().add(timeHeader);

            //spacing
            rankingColumn.getChildren().add(new Text(""));
            nameColumn.getChildren().add(new Text(""));
            scoreColumn.getChildren().add(new Text(""));
            timeColumn.getChildren().add(new Text(""));

            rankingColumn.setAlignment(Pos.TOP_CENTER);
            nameColumn.setAlignment(Pos.TOP_CENTER);
            scoreColumn.setAlignment(Pos.TOP_CENTER);
            timeColumn.setAlignment(Pos.TOP_CENTER);
            //VBOX ADD ENTRIES
            int count = 1; //counter for rank
            for(String s: scoreEntries){
                String[] strArray = s.split(",");
                Text rank = new Text(String.valueOf(count)), name = new Text(strArray[0]), score = new Text(strArray[1]), time = new Text(strArray[2]);

                rank.setFont(font);
                rank.setFill(Color.BLUEVIOLET);

                name.setFont(font);
                name.setFill(Color.BLUEVIOLET);

                score.setFont(font);
                score.setFill(Color.BLUEVIOLET);

                time.setFont(font);
                time.setFill(Color.BLUEVIOLET);

                rankingColumn.getChildren().add(rank);
                nameColumn.getChildren().add(name);
                scoreColumn.getChildren().add(score);
                timeColumn.getChildren().add(time);
                count++;
            }

            //assign children to the 3 divisions of the leaderboard pane.
            upperHBox.getChildren().addAll(leaderBoardLogo);
            middleHBox.getChildren().addAll(rankingColumn, nameColumn, scoreColumn, timeColumn);
            lowerHBox.getChildren().addAll(leftButtonColumn, rightButtonColumn);
            leftButtonColumn.getChildren().add(mainMenu);
            rightButtonColumn.getChildren().add(exit);

            pane.getChildren().addAll(upperHBox, middleHBox, lowerHBox);

        }

        public Pane getPane(){
            return this.pane;
        }

        public Scene getScene(){
            return this.scene;
        }

    }

    //============================================================================================================


    //=======================================MAIN LAUNCHER========================================================
    public static void main(String[] args) {
        launch();
    }
    //============================================================================================================

}