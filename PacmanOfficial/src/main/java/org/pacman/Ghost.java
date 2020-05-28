package org.pacman;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class Ghost {

    //===========================================STATE=======================================================
    private boolean vulnerable = false;
    private boolean respawn = false;
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
    private String shorterMovement = "";
    private String longerMovement = "";


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
    private boolean colorFearful;
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

    public void lookForPlayer(){//how enemy will update it's knowledge of where the player is.
            if(getDistanceFromPlayer(this.xMid, this.yMid, pacman.getCenterX(), pacman.getCenterY()) < (cellOccupied.getSize() * 5)){
                moveIntelligently();
            }else{
                moveRandomly();
            }
    }

    public double getDistanceFromPlayer(double ghostX, double ghostY, double pacmanX, double pacmanY){
        double xDistance = Math.abs(ghostX - pacmanX);
        double yDistance = Math.abs(ghostY - pacmanY);
        return Math.sqrt((xDistance*xDistance) + (yDistance * yDistance));
    }

    public void moveRandomly(){
        currentCanMove = executeDirection(currentDirection);
        nextCanMove = executeDirection(nextDirection);

        if(nextCanMove){
            currentDirection = nextDirection;
            chooseNextDirection();
        }
        if(!currentCanMove && !nextCanMove){
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
    private void moveIntelligently(){
            double xDistance = pacman.getCenterX() - this.xMid;
            double yDistance = pacman.getCenterY() - this.yMid;

            if(xDistance == 0){
                //moveVertically
            }else if(yDistance == 0){
                //moveHorizontally
            }

            if(Math.abs(xDistance) < Math.abs(yDistance)){//shorter distance is on the x axis
                if(xDistance < 0){
                    shorterMovement = "LEFT";
                }
                if(yDistance < 0){
                    shorterMovement = "UP";
                }
            }else{//shorter distance is on the y axis

            }


    }

    private void moveHorizontally(){

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

    public void makeVulnerable(){
        vulnerable = true;
        if(!colorFearful){//prevents duplicate coloring when pacman eats a booster before this returns to normal.
            colorFearful();
        }
    }

    public void setToNormalMode(){
        vulnerable = false;
        returnToNormalColor();
    }

    public void respawn(MapCell cell){
        this.cellOccupied = cell;
        respawn = true;
        for(Node node: BODY_PARTS){
            node.setLayoutX(0);
            node.setLayoutY(0);
            node.setTranslateY(0);
            node.setTranslateY(0);
        }
        updateBounds();
    }

    public boolean needsToRespawn(){
        return respawn;
    }

    public void countDownRespawn(){
        respawnTimer--;
    }

    public int getRespawnTime(){
        return respawnTimer;
    }

    public void resetSpawn(){
        respawn = false;
        respawnTimer = 420;
    }

    public void ghostWarning(){
        if(colorFearful){
            returnToNormalColor();
        }else{
            colorFearful();
        }
    }

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

    public void colorFearful(){
        body.setFill(Color.BLUE);
        ghostHead.setFill(Color.BLUE);
        leftPupil.setCenterX(leftPupil.getCenterX() - (leftPupil.getRadius() * .7));
        rightPupil.setCenterX(rightPupil.getCenterX() + (rightPupil.getRadius() * .7));

        rightPupil.setCenterY(rightPupil.getCenterY() - rightPupil.getRadius() * .8);
        leftPupil.setCenterY(leftPupil.getCenterY() - leftPupil.getRadius() * .8);
        //make all these basically invisible

        leftEyeLid.setFill(Color.BLUE);
        rightEyeLid.setFill(Color.BLUE);
        rightEyeLidLiner.setFill(Color.BLACK);
        leftEyeLidLiner.setFill(Color.BLACK);
        sadMouth.setFill(Color.BLACK);
        colorFearful = true;
    }

    private void returnToNormalColor(){
        ghostHead.setFill(this.color);
        body.setFill(this.color);

        leftPupil.setCenterX(leftPupil.getCenterX() + (leftPupil.getRadius() * .7));
        rightPupil.setCenterX(rightPupil.getCenterX() - (rightPupil.getRadius() * .7));
        leftPupil.setCenterY(leftPupil.getCenterY() + leftPupil.getRadius() * .8);
        rightPupil.setCenterY(rightPupil.getCenterY() + rightPupil.getRadius() * .8);

        leftEyeLid.setFill(Color.WHITE);
        rightEyeLid.setFill(Color.WHITE);
        rightEyeLidLiner.setFill(Color.WHITE);
        leftEyeLidLiner.setFill(Color.WHITE);
        rightEyeLiner.setFill(Color.BLACK);
        leftEyeLiner.setFill(Color.BLACK);

        //make invisible again.
        sadMouth.setFill(this.color);
        colorFearful = false;
    }

    public LinkedList<Node> getBody(){
        return BODY_PARTS;
    }
//==============================================================================================================
}
