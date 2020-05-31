package org.pacman;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;

import java.util.LinkedList;
import java.util.Random;

public class Ghost {

    //===========================================STATE=======================================================
    private boolean vulnerable = false;
    private boolean alive = true;
    private int respawnTimer = 420;
    //=======================================================================================================

    //====================================DIRECTIONAL CONSTANTS==============================================
    private final static String LEFT = "LEFT";
    private final static String RIGHT = "RIGHT";
    private final static String UP = "UP";
    private final static String DOWN = "DOWN";
    //=============================================BODY=====================================================

    //=====================================NAVIGATIONAL VARIABLES===========================================
    private MapCell cellOccupied;
    private final Pacman pacman; //used for checking whereabouts of player.
    private double xLeft;
    private double xRight;
    private double yUp;
    private double yDown;
    private double xMid;
    private double yMid;
    private final double distance;

    private final LinkedList<String> RANDOM_DIRECTIONS = new LinkedList<>();
    private final Random RANDOM = new Random();
    private String currentDirection;
    private String nextDirection;
    private boolean currentCanMove;
    private boolean nextCanMove;
    //======================================================================================================

    //intelligent movement variables
    private final String[] smartDirections = new String[3];
    private String lastMove;
    private boolean lastMoveWasRandom = false;
    private static double trackingDistance = 15;
    private final LinkedList<Node> BODY_PARTS = new LinkedList<>();


    //USED FOR DRAWING
    private final Color[] colors = {Color.BLUE, Color.RED, Color.ORANGE, Color.FUCHSIA, Color.GREEN, Color.VIOLET};
    private Arc ghostHead;
    private Polygon body;
    private Circle leftEye;
    private Circle leftEyeLiner;
    private Circle rightEyeLiner;
    private Circle rightEye;
    private Circle leftPupil;
    private Circle rightPupil;
    private Arc leftEyeLid;
    private Arc rightEyeLid;
    private Arc leftEyeLidLiner;
    private Arc rightEyeLidLiner;
    private Color color;
    private Arc sadMouth;
    private boolean colorVulnerable = false;
    //======================================================================================================

    public Ghost(MapCell cell, Pacman pacman, double distance){
        this.pacman = pacman;
        this.cellOccupied = cell;
        this.distance = distance;

        initGhost();
        updateBounds();
        initRandomDirectionList();
        chooseCurrentDirection();
        chooseNextDirection();
    }

    public void move(){//how enemy will update it's knowledge of where the player is.\
            if(horizontallyCentered() && verticallyCentered()){
                findBestPaths(); //re-evaluate the best path each time you are in the center of a cell.
            }
            if(vulnerable || !pacmanIsInRange()){ //this structure is purposeful to allow the ghost to move in the opposite direction IF they were previously NOT in range or were just vulnerable..
                moveRandomly();
                lastMoveWasRandom = true;
            }else{
                if(!moveIntelligently()){
                    moveRandomly();
                }
                lastMoveWasRandom = false;
            }
    }

    private boolean pacmanIsInRange(){
        return getDistanceFromPlayer(this.xMid, this.yMid, pacman.getCenterX(), pacman.getCenterY()) < (cellOccupied.getSize() * trackingDistance);
    }

    private double getDistanceFromPlayer(double ghostX, double ghostY, double pacmanX, double pacmanY){
        double xDistance = Math.abs(ghostX - pacmanX);
        double yDistance = Math.abs(ghostY - pacmanY);
        return Math.sqrt((xDistance*xDistance) + (yDistance * yDistance));
    }

    private void moveRandomly(){
        nextCanMove = executeDirection(nextDirection); //interesting that I call both of these.
        currentCanMove = executeDirection(currentDirection);

        if(nextCanMove){
            lastMove = nextDirection; //sets the last move for the intelligent decision to analyze.
            currentDirection = nextDirection;
            chooseNextDirection();
        }else if(currentCanMove){
            lastMove = currentDirection;
        }else{
            chooseCurrentDirection();
            chooseNextDirection();
        }
    }

    //=====================================RANDOM MOVEMENT|
    private void initRandomDirectionList(){
        RANDOM_DIRECTIONS.add(LEFT);
        RANDOM_DIRECTIONS.add(RIGHT);
        RANDOM_DIRECTIONS.add(UP);
        RANDOM_DIRECTIONS.add(DOWN);

    }

    private void chooseCurrentDirection(){
        currentDirection = RANDOM_DIRECTIONS.get(RANDOM.nextInt(RANDOM_DIRECTIONS.size()));
    }

    private void chooseNextDirection(){
        do{
            nextDirection = RANDOM_DIRECTIONS.get(RANDOM.nextInt(RANDOM_DIRECTIONS.size()));
        }while(oppositeDirection(nextDirection, currentDirection));
    }

    private boolean oppositeDirection(String next, String current){
        if(next == null || current == null){
            return false;
        }
        switch (next){
            case UP:
                if(current.equals(DOWN)){
                    return true;
                }
                break;
            case DOWN:
                if(current.equals(UP)){
                    return true;
                }
                break;
            case LEFT:
                if(current.equals(RIGHT)){
                    return true;
                }
                break;
            case RIGHT:
                if(current.equals(LEFT)){
                    return true;
                }
        }
        return false;
    }
    //===================================================|

    //===================================INTELLIGENT MOVEMENT|
    private boolean moveIntelligently(){
        for(String direction: smartDirections){ //go through the preferred directions in the direction array (calculated from the findBestPaths method).
            if(direction != null && !oppositeDirection(direction, lastMove)){ //cannot move if direction is opposite of his last direction to prevent excessive backtracking over and over.
                if(executeDirection(direction)){
                    lastMove = direction; //record the last direction moved
                    return true;
                }
            }else if((smartDirections[0] != null) && lastMoveWasRandom && executeDirection(smartDirections[0])){
                lastMove = smartDirections[0];
                return true;
            }
        }
        return false;
    }

    private void findBestPaths(){
        String lineOfSight = getLineOfSight();
        String[] shortAndLong = getShortAndLong();

        smartDirections[0] = lineOfSight; //this path is the highest priority
        smartDirections[1] = shortAndLong[0]; //second priority is to close down the longest distance.
        smartDirections[2] = shortAndLong[1]; //3rd priority is to close down the shorter distance. (found that pursuit was most logical this way).
    }

    private String getLineOfSight(){
        if(pacman.getCenterX() == xMid){
            if(pacmanIsAbove()){
                return UP;
            }else{
                return DOWN;
            }
        }else if(pacman.getCenterY() == yMid){
            if(pacmanOnLeft()){
                return LEFT;
            }else{
                return RIGHT;
            }
        }
        return null;
    }

    private boolean pacmanOnLeft(){
        return pacman.getCenterX() - xMid < 0;
    }

    private boolean pacmanIsAbove(){
        return pacman.getCenterY() - yMid < 0;
    }

    private String[] getShortAndLong(){
        double xDistance = pacman.getCenterX() - xMid;
        double yDistance = pacman.getCenterY() - yMid;

        if (Math.abs(xDistance) == Math.abs(yDistance)) {
            return randomOrderChoice(xDistance, yDistance);
        }else if(Math.abs(xDistance) < Math.abs(yDistance)) {
            return new String[]{getVerticalDirection(yDistance), getHorizontalDirection(xDistance)}; //vertical movement is priority
        }else{
            return new String[]{getHorizontalDirection(xDistance), getVerticalDirection(yDistance)}; //horizontal movement is priority
        }
    }

    private String[] randomOrderChoice(double x, double y){
        String horizontalDirection = getHorizontalDirection(x);
        String verticalDirection = getVerticalDirection(y);

        int randomChoice = new Random().nextInt(2);
        if(randomChoice == 0){
            return new String[]{horizontalDirection, verticalDirection};
        }else{
            return new String[]{verticalDirection, horizontalDirection};
        }
    }

    private String getHorizontalDirection(double x){
        if(x < 0){
            return LEFT;
        }else{
            return RIGHT;
        }
    }

    private String getVerticalDirection(double y){
        if(y < 0) {
            return UP;
        }else{
            return DOWN;
        }
    }

    public static void increaseTrackingDistance(){
        trackingDistance += .5;
    }

    public static void resetTrackingDistance(){
        trackingDistance = 4;
    }

    //===================================================|

    //======================================NAVIGATIONAL METHODS============================================
    private boolean executeDirection(String direction){
        switch(direction){
            case LEFT:
                return moveLeft();
            case RIGHT:
                return moveRight();
            case UP:
                return moveUp();
            case DOWN:
                return moveDown();
            default:
                return false;
        }
    }

    private void moveUnit(String direction){
        double horizontalMovement = 0;
        double verticalMovement = 0;
        switch (direction) {
            case LEFT:
                horizontalMovement = -distance;
                break;

            case RIGHT:
                horizontalMovement = distance;
                break;
            case UP:
                verticalMovement = -distance;
                break;
            case DOWN:
                verticalMovement = distance;
                break;
            default:
                System.out.println("No valid option selected");
        }
        for(Node node: BODY_PARTS){
            node.setLayoutX(node.getLayoutX() + horizontalMovement);
            node.setLayoutY(node.getLayoutY() + verticalMovement);
        }

        updateBounds(); //update bounds reference for collision detection.
    }

    private boolean moveLeft(){
        boolean moved = false;
        if(verticallyCentered() && !hitLeftBorder()){
            moveUnit(LEFT);
            moved = true;
            if(movedToCellLeft()){
                cellOccupied = cellOccupied.getLeftNeighbor();
            }
        }
        return moved;
    }

    private boolean moveRight(){
        boolean moved = false;
        if(verticallyCentered() && !hitRightBorder()){
            moveUnit(RIGHT);
            moved = true;
            if(movedToCellRight()){
                cellOccupied = cellOccupied.getRightNeighbor();
            }
        }
        return moved;
    }

    private boolean moveUp(){
        boolean moved = false;
        if(horizontallyCentered() && !hitTopBorder()){
            moveUnit(UP);
            moved = true;
            if(movedToCellAbove()){
                cellOccupied = cellOccupied.getTopNeighbor();
            }
        }
        return moved;
    }

    private boolean moveDown(){
        boolean moved = false;
        if(horizontallyCentered() && !hitBottomBorder()){
            moveUnit(DOWN);
            moved = true;
            if(movedToCellBelow()){
                cellOccupied = cellOccupied.getBottomNeighbor();
            }
        }
        return moved;

    }

    private boolean movedToCellAbove(){
        return yUp < cellOccupied.getTopNeighbor().getY() + cellOccupied.getSize();
    }

    private boolean movedToCellBelow(){
        return yDown > cellOccupied.getBottomNeighbor().getY();
    }

    private boolean movedToCellRight(){
        return xRight > cellOccupied.getRightNeighbor().getX();
    }

    private boolean movedToCellLeft(){
        return xLeft < cellOccupied.getLeftNeighbor().getX() + cellOccupied.getSize();
    }

    private boolean hitLeftBorder(){
        return xLeft <= cellOccupied.getLeftNeighbor().getX() + cellOccupied.getSize() && (cellOccupied.getLeftNeighbor().isBorder() || cellOccupied.getLeftNeighbor().isPortal());
    }

    private boolean hitRightBorder(){
        return xRight >= cellOccupied.getRightNeighbor().getX() && (cellOccupied.getRightNeighbor().isBorder() || cellOccupied.getRightNeighbor().isPortal());
    }

    private boolean hitTopBorder(){
        return yUp <= cellOccupied.getTopNeighbor().getY() + cellOccupied.getSize() && (cellOccupied.getTopNeighbor().isBorder()  || cellOccupied.getTopNeighbor().isPortal());
    }

    private boolean hitBottomBorder(){
        return yDown >= cellOccupied.getBottomNeighbor().getY() && (cellOccupied.getBottomNeighbor().isBorder() || cellOccupied.getBottomNeighbor().isPortal());
    }

    public boolean collidedWithPlayer(){
        return collidedHorizontally() && collidedVertically();
    }

    public boolean collidedHorizontally(){
        return (xLeft <= pacman.getCenterX() && xLeft >= pacman.getCenterX() - pacman.getRadiusX()) || (xRight >= pacman.getCenterX() && xRight <= pacman.getCenterX() + pacman.getRadiusX());
    }

    public boolean collidedVertically(){
        return ((yUp <= pacman.getCenterY() && yUp >= pacman.getCenterY() - pacman.getRadiusY()) || (yDown >= pacman.getCenterY() && yDown <= pacman.getCenterY() + pacman.getRadiusY()));
    }

    private boolean verticallyCentered(){
        return yMid == cellOccupied.getCenterY();
    }

    private boolean horizontallyCentered(){
        return xMid == cellOccupied.getCenterX();
    }

    private void updateBounds(){
        xLeft = ghostHead.getCenterX() - ghostHead.getRadiusX() + ghostHead.getLayoutX();
        xRight = ghostHead.getCenterX() + ghostHead.getRadiusX() + ghostHead.getLayoutX();
        yUp = ghostHead.getCenterY() - ghostHead.getRadiusY() + ghostHead.getLayoutY();
        yDown = ghostHead.getCenterY() + ghostHead.getRadiusY() + ghostHead.getLayoutY();
        xMid = (xLeft + xRight) / 2;
        yMid = (yUp + yDown) / 2;
    }
    //==========================================================================================================

    //=========================================VULNERABILITY MODE METHODS=======================================
    public boolean isVulnerable(){
        return vulnerable;
    }

    public void setVulnerable(){
        vulnerable = true;
        if(!colorVulnerable && alive){//prevents coloring dead and recoloring pacman eats a booster before this returns to normal.
            setColorVulnerable();
            colorVulnerable = true;
        }
    }

    public void setNotVulnerable(){
        vulnerable = false;
        if(alive){
            setColorAlive();
        }
        colorVulnerable = false;
    }

    public void kill(){
        alive = false;
        setColorDead();
        this.cellOccupied = Map.getGhostStartingPosition(); //set cell back at spawn
        for(Node node: BODY_PARTS){
            node.setLayoutX(0);
            node.setLayoutY(0);
            node.setTranslateY(0);
            node.setTranslateY(0);
        }
        updateBounds();
    }

    public boolean tryRespawn(){
        if(respawnTimer == 0){
            if(vulnerable){
                setColorVulnerable();
            }else{
                setColorAlive();
            }
            alive = true;
            respawnTimer = 420;
        }else{
            respawnTimer--;
        }
        return alive;
    }

    public boolean isAlive(){
        return alive;
    }

    public void showWarning(){//cycles colors to warn user that he is about to turn back to normal
        if(alive){
            if(colorVulnerable){ //switch back to normal
                setColorAlive();
                colorVulnerable = false;
            }else{ //switch back to vulnerable
                setColorVulnerable();
                colorVulnerable = true;
            }
        }

    }
    //==========================================================================================================

    //======================================CONSTRUCT THE ENEMY BODY============================================
    private void initGhost() {
        initColor();
        initBody();
        initHead();
        initEyes();
        initPupils();
        initSadMouth();
    }

    private void initColor() {
        color = getRandomColor();
    }

    private void initBody(){
        double halfPoint = cellOccupied.getSize() / 6;
        //first triangle
        double xPoint1 = cellOccupied.getX();
        double yPoint1 = cellOccupied.getY() + cellOccupied.getSize() - halfPoint;

        double xPoint2 = cellOccupied.getX() + halfPoint;
        double yPoint2 = cellOccupied.getY() + cellOccupied.getSize();

        double xPoint3 = cellOccupied.getX() + halfPoint * 2;
        double yPoint3 = cellOccupied.getY() + cellOccupied.getSize() - halfPoint;
        //second triangle
        double xPoint4 = cellOccupied.getX() + 3 * halfPoint;
        double yPoint4 = cellOccupied.getY() + cellOccupied.getSize();

        double xPoint5 = cellOccupied.getX() + 4 * halfPoint;
        double yPoint5 = cellOccupied.getY() + cellOccupied.getSize() - halfPoint;

        //third triangle
        double xPoint6 = cellOccupied.getX() + 5 * halfPoint;
        double yPoint6 = cellOccupied.getY() + cellOccupied.getSize();

        double xPoint7 = cellOccupied.getX() + 6 * halfPoint;
        double yPoint7 = cellOccupied.getY() + cellOccupied.getSize() - halfPoint;
        //top rectangle
        double xPoint8 = cellOccupied.getX() + cellOccupied.getSize();
        double yPoint8 = cellOccupied.getCenterY();
        double xPoint9 = cellOccupied.getX();
        double yPoint9 = cellOccupied.getCenterY();

        body = new Polygon(xPoint1, yPoint1, xPoint2, yPoint2, xPoint3, yPoint3, xPoint4, yPoint4,
                xPoint5, yPoint5, xPoint6, yPoint6, xPoint7, yPoint7, xPoint8, yPoint8, xPoint9, yPoint9);
        body.setFill(this.color);
        BODY_PARTS.add(body);
    } //this method looks like hell. fix later if possible

    private void initHead(){
        ghostHead = new Arc();
        ghostHead.setType(ArcType.ROUND);
        ghostHead.setStartAngle(0);
        ghostHead.setLength(180);
        ghostHead.setCenterX(cellOccupied.getCenterX());
        ghostHead.setCenterY(cellOccupied.getCenterY());
        ghostHead.setRadiusX(cellOccupied.getSize() / 2);
        ghostHead.setRadiusY(cellOccupied.getSize() / 2);
        ghostHead.setFill(color);
        BODY_PARTS.add(ghostHead);
    }

    private void initEyes(){
        double xLeftCoordinate = ghostHead.getCenterX() - ((ghostHead.getRadiusX() * 4 / 9));
        double xRightCoordinate = ghostHead.getCenterX() + ((ghostHead.getRadiusX() * 4 / 9));
        double yCoordinate = ghostHead.getCenterY() - ghostHead.getRadiusY() / 2.5;

        leftEyeLiner = new Circle(xLeftCoordinate, yCoordinate, cellOccupied.getSize() / 5 + 1);
        leftEyeLiner.setFill(Color.BLACK);

        leftEye = new Circle(xLeftCoordinate, yCoordinate, cellOccupied.getSize() / 5);
        leftEye.setFill(Color.WHITE);

        rightEyeLiner = new Circle(xRightCoordinate, yCoordinate, cellOccupied.getSize() / 5 + 1);
        rightEyeLiner.setFill(Color.BLACK);

        rightEye = new Circle(xRightCoordinate, yCoordinate, cellOccupied.getSize() / 5);
        rightEye.setFill(Color.WHITE);

        leftEyeLidLiner = new Arc();
        leftEyeLidLiner.setCenterX(xLeftCoordinate);
        leftEyeLidLiner.setCenterY(yCoordinate + (.15 * leftEye.getRadius()));
        leftEyeLidLiner.setRadiusX(leftEye.getRadius() - leftEye.getRadius()*.05);
        leftEyeLidLiner.setRadiusY(leftEye.getRadius() - (.2 * leftEye.getRadius()));
        leftEyeLidLiner.setType(ArcType.ROUND);
        leftEyeLidLiner.setFill(Color.WHITE);
        leftEyeLidLiner.setStartAngle(30);
        leftEyeLidLiner.setLength(-180);

        rightEyeLidLiner = new Arc();
        rightEyeLidLiner.setCenterX(xRightCoordinate);
        rightEyeLidLiner.setCenterY(yCoordinate + (.15 * rightEye.getRadius()));
        rightEyeLidLiner.setRadiusX(rightEye.getRadius() - rightEye.getRadius()*.05);
        rightEyeLidLiner.setRadiusY(rightEye.getRadius() - (.2 * rightEye.getRadius()));
        rightEyeLidLiner.setType(ArcType.ROUND);
        rightEyeLidLiner.setFill(Color.WHITE);
        rightEyeLidLiner.setStartAngle(150);
        rightEyeLidLiner.setLength(180);

        leftEyeLid = new Arc();
        leftEyeLid.setCenterX(xLeftCoordinate);
        leftEyeLid.setCenterY(yCoordinate + (.3 * leftEye.getRadius()));
        leftEyeLid.setRadiusX(leftEye.getRadius() - leftEye.getRadius()*.05);
        leftEyeLid.setRadiusY(leftEye.getRadius() - (.20 * leftEye.getRadius()));
        leftEyeLid.setType(ArcType.ROUND);
        leftEyeLid.setFill(Color.WHITE);
        leftEyeLid.setStartAngle(30);
        leftEyeLid.setLength(-180);

        rightEyeLid = new Arc();
        rightEyeLid.setCenterX(xRightCoordinate);
        rightEyeLid.setCenterY(yCoordinate + (.3 * rightEye.getRadius()));
        rightEyeLid.setRadiusX(rightEye.getRadius() - rightEye.getRadius()*.05);
        rightEyeLid.setRadiusY(rightEye.getRadius() - (.20 * rightEye.getRadius()));
        rightEyeLid.setType(ArcType.ROUND);
        rightEyeLid.setFill(Color.WHITE);
        rightEyeLid.setStartAngle(150);
        rightEyeLid.setLength(180);


        BODY_PARTS.add(rightEyeLiner);
        BODY_PARTS.add(leftEyeLiner);
        BODY_PARTS.add(leftEye);
        BODY_PARTS.add(rightEye);

        BODY_PARTS.add(rightEyeLidLiner);
        BODY_PARTS.add(leftEyeLidLiner);
        BODY_PARTS.add(rightEyeLid);
        BODY_PARTS.add(leftEyeLid);
    }

    private void initPupils(){
        leftPupil = new Circle(leftEye.getCenterX(), leftEye.getCenterY() - (leftEye.getRadius() / 8), leftEye.getRadius() * 1 / 3);
        leftPupil.setFill(Color.BLUE);
        rightPupil = new Circle(rightEye.getCenterX(), rightEye.getCenterY() - (rightEye.getRadius() / 8), rightEye.getRadius() * 1 / 3);
        rightPupil.setFill(Color.BLUE);
        BODY_PARTS.add(leftPupil);
        BODY_PARTS.add(rightPupil);
    }

    private void initSadMouth(){
        sadMouth = new Arc();
        sadMouth.setLength(180);
        sadMouth.setStartAngle(0);
        sadMouth.setType(ArcType.ROUND);
        sadMouth.setRadiusY(ghostHead.getRadiusY() / 2);
        sadMouth.setRadiusX(ghostHead.getRadiusX() / 1.5);
        sadMouth.setCenterY(ghostHead.getCenterY() + ghostHead.getRadiusY() / 1.7);
        sadMouth.setCenterX(ghostHead.getCenterX());
        sadMouth.setFill(this.color); //blend in until it needs to be shown.
        BODY_PARTS.add(sadMouth);
    }

    private Color getRandomColor(){
        Random random = new Random();
        return colors[random.nextInt(colors.length - 1)];
    }

    private void setColorAlive(){
        ghostHead.setFill(this.color);
        body.setFill(this.color);
        leftEyeLid.setFill(Color.WHITE);
        rightEyeLid.setFill(Color.WHITE);
        leftEye.setFill(Color.WHITE);
        rightEye.setFill(Color.WHITE);
        rightEyeLidLiner.setFill(Color.WHITE);
        leftEyeLidLiner.setFill(Color.WHITE);
        rightEyeLiner.setFill(Color.BLACK);
        leftEyeLiner.setFill(Color.BLACK);
        leftPupil.setFill(Color.BLUE);
        rightPupil.setFill(Color.BLUE);
        sadMouth.setFill(this.color);
        colorVulnerable = false;
    }

    private void setColorVulnerable(){
        body.setFill(Color.BLUE);
        ghostHead.setFill(Color.BLUE);
        leftEye.setFill(Color.WHITE);
        rightEye.setFill(Color.WHITE);
        rightEyeLiner.setFill(Color.BLACK);
        leftEyeLiner.setFill(Color.BLACK);
        leftEyeLid.setFill(Color.BLUE);
        rightEyeLid.setFill(Color.BLUE);
        rightPupil.setFill(Color.BLUE);
        leftPupil.setFill(Color.BLUE);
        rightEyeLidLiner.setFill(Color.BLACK);
        leftEyeLidLiner.setFill(Color.BLACK);
        sadMouth.setFill(Color.BLACK);
        colorVulnerable = true;
    }

    private void setColorDead(){
        for(Node node: BODY_PARTS){
            if(node instanceof Arc){
                ((Arc) node).setFill(null);
            }else if(node instanceof Circle){
                ((Circle) node).setFill(null);
            }else{
                ((Polygon) node).setFill(null);
            }
        }
    }

    public LinkedList<Node> getBody(){
        return BODY_PARTS;
    }
//==============================================================================================================
}
