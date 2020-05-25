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
    //Constants for direction
    private final static String LEFT = "LEFT";
    private final static String RIGHT = "RIGHT";
    private final static String UP = "UP";
    private final static String DOWN = "DOWN";
    //=============================================BODY=====================================================

    //USED FOR NAVIGATION
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

    //intelligent movement variables
    private String shorterMovement = "";
    private String longerMovement = "";

    private final LinkedList<Node> BODY_PARTS = new LinkedList<>();

    //USED FOR DRAWING
    private final Color[] colors = {Color.BLUE, Color.RED, Color.ORANGE, Color.FUCHSIA, Color.GREEN, Color.VIOLET};
    private Arc ghostHead;
    private Polygon body;
    private Circle leftEye;
    private Circle rightEye;
    private Color color;
    //======================================================================================================

    public Ghost(MapCell cell, Pacman pacman, double distance){
        this.pacman = pacman;
        this.cellOccupied = cell;
        this.distance = distance;

        drawGhost();
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

    //======================================NAVIGATIONAL METHODS============================================

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

    //======================================================================================================


    //======================================CONSTRUCT THE ENEMY BODY============================================
    private void drawGhost(){
        initColor();
        initBody();
        initHead();
        initEyes();
        initPupils();
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
        double yCoordinate = ghostHead.getCenterY() - ghostHead.getRadiusY() / 4;
        Circle leftEyeLiner = new Circle(xLeftCoordinate, yCoordinate, cellOccupied.getSize() / 5 + 1);
        leftEyeLiner.setFill(Color.BLACK);

        leftEye = new Circle(xLeftCoordinate, yCoordinate, cellOccupied.getSize() / 5);
        leftEye.setFill(Color.WHITE);

        Circle rightEyeLiner = new Circle(xRightCoordinate, yCoordinate, cellOccupied.getSize() / 5 + 1);
        rightEyeLiner.setFill(Color.BLACK);

        rightEye = new Circle(xRightCoordinate, yCoordinate, cellOccupied.getSize() / 5);
        rightEye.setFill(Color.WHITE);

        BODY_PARTS.add(rightEyeLiner);
        BODY_PARTS.add(leftEyeLiner);
        BODY_PARTS.add(leftEye);
        BODY_PARTS.add(rightEye);

    }

    private void initPupils(){
        Circle leftPupil = new Circle(leftEye.getCenterX(), leftEye.getCenterY(), leftEye.getRadius() * 1 / 7);
        leftPupil.setFill(Color.BLUE);
        Circle rightPupil = new Circle(rightEye.getCenterX(), rightEye.getCenterY(), rightEye.getRadius() * 1 / 7);
        rightPupil.setFill(Color.BLUE);
        BODY_PARTS.add(leftPupil);
        BODY_PARTS.add(rightPupil);
    }

    private Color getRandomColor(){
        Random random = new Random();
        return colors[random.nextInt(colors.length - 1)];
    }

    public LinkedList<Node> getBody(){
        return BODY_PARTS;
    }
//==============================================================================================================
}
