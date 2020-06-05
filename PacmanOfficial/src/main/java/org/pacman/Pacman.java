package org.pacman;

import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;

import javax.swing.*;

public class Pacman extends Arc {


    //static final variables for animation and movement.
    private static final String RIGHT = "RIGHT";
    private static final String LEFT = "LEFT";
    private static final String DOWN = "DOWN";
    private static final String UP = "UP";
    private static final double MOUTH_SPEED = 10; //degrees of closure per frame
    private static final double ANGLE_LEFT = 135;
    private static final double ANGLE_RIGHT = 315;
    private static final double ANGLE_UP = 45;
    private static final double ANGLE_DOWN = 225;
    private static final double MOUTH_OPEN = 270;
//    private static final double DISTANCE = 4; // for speed variation.
    private double distance;

    private final double OFFSET_ZERO; //COMPENSATES FOR THE FRAME IN THE TOP ROW. should be equal to the cell size (or radius * 2)
    private MapCell startingPosition;

    //state variables for animation changes
    private boolean closing = true;
    private double arcLength = 270;// the larger this is, the smaller the mouth opening //based on updateFunction. --> increment / decrement by MOUTH_SPEED
    private double startAngle = ANGLE_RIGHT;

    //reference point for current location
    private MapCell cellOccupied; //for current location

    private boolean invincible = false;

    //===================================CONSTRUCTORS============================================================
    public Pacman(double x, double y, double r){
        super(x, y, r, r, ANGLE_LEFT, MOUTH_OPEN);
        super.setType(ArcType.ROUND);
        super.setFill(Color.YELLOW);

//        soundEffects = new AudioLibrary();
        OFFSET_ZERO = r * 4;
    }

    public Pacman(MapCell cell, double speed){
        this(cell.getCenterX(), cell.getCenterY(), cell.getSize() / 2);
        this.startingPosition = cell;
        updateMapPosition(cell);
        this.distance = speed;
        cellOccupied.removeCellFood(); //initialize the starting cell to already be eaten.
        Statistics.increaseScore(10);
    }
    //===========================================================================================================

    //===================================NORMAL MOVEMENT METHODS=================================================
    public boolean moveRight(){
        boolean moved = false;
        if(verticallyCentered()) {
            if(cellOccupied.isPortal()){
                if(!cellOccupied.getRightNeighbor().isBorder()){//PREVENTS MOVING INTO BORDER WHILE GOING THROUGH PORTAL
                    super.setCenterX(getCenterX() + distance);
                    moveRightFromPortal();
                    moved = true;
                }
            }else{
                if(!hitRightBorder()){
                    super.setCenterX(getCenterX() + distance);
                    moved = true;
                    if (movedToCellRight()) {
                        updateMapPosition(cellOccupied.getRightNeighbor());
                    }
                }
            }
        }
        if (foodInReach(RIGHT)) {
            eatFood(cellOccupied);
        }
        if(moved){
            mouthAction(RIGHT);
        }
        return moved;
    }

    public boolean moveLeft(){
        boolean moved = false;
        if(verticallyCentered()){
            if(cellOccupied.isPortal()) {
                if(!cellOccupied.getLeftNeighbor().isBorder()){//PREVENTS MOVING INTO BORDER WHILE GOING THROUGH PORTAL
                    super.setCenterX(getCenterX() - distance);
                    moveLeftFromPortal();
                    moved = true;
                }
            }else{
                if(!hitLeftBorder()){
                    super.setCenterX(getCenterX() - distance);
                    if (movedToCellLeft()){
                        updateMapPosition(cellOccupied.getLeftNeighbor());
                    }
                    moved = true;
                }
            }
        }
        if(foodInReach(LEFT)) {
            eatFood(cellOccupied);
        }
        if(moved){
            mouthAction(LEFT);
        }
        return moved;
    }

    public boolean moveUp() {
        boolean moved = false;
        if(horizontallyCentered()) {
            if(cellOccupied.isPortal()){
                if(!cellOccupied.getTopNeighbor().isBorder()){//PREVENTS MOVING INTO BORDER WHILE GOING THROUGH PORTAL
                    super.setCenterY(getCenterY() - distance);
                    moveUpFromPortal();
                    moved = true;
                }
            }else{
                if (!hitTopBorder()) {
                    super.setCenterY(getCenterY() - distance);
                    if (movedToCellAbove()){
                        updateMapPosition(cellOccupied.getTopNeighbor());
                    }
                    moved = true;
                }
            }
        }
        if (foodInReach(UP)) {
            eatFood(cellOccupied);
        }
        if(moved){
            mouthAction(UP);
        }
        return moved;
    }

    public boolean moveDown() {
        boolean moved = false;
        if (horizontallyCentered()) {
            if(cellOccupied.isPortal()){
                if(!cellOccupied.getBottomNeighbor().isBorder()) {//PREVENTS MOVING INTO BORDER WHILE GOING THROUGH PORTAL
                    super.setCenterY(getCenterY() + distance);
                    moveDownFromPortal();
                    moved = true;
                }
            }else{
                if(!hitBottomBorder()) {
                    super.setCenterY(getCenterY() + distance);
                    if (movedToCellBelow()) {
                        updateMapPosition(cellOccupied.getBottomNeighbor());
                    }
                    moved = true;
                }
            }
        }
        if (foodInReach(DOWN)) {
            eatFood(cellOccupied);
        }
        if(moved){
            mouthAction(DOWN);
        }
        return moved;
    }

    private boolean verticallyCentered(){
        return super.getCenterY() == cellOccupied.getCenterY();
    }

    private boolean horizontallyCentered(){
        return super.getCenterX() == cellOccupied.getCenterX();
    }

    private boolean hitBottomBorder(){
        return this.getCenterY() + getRadiusY() >= cellOccupied.getBottomNeighbor().getY()  && cellOccupied.getBottomNeighbor().isBorder();
    }

    private boolean hitTopBorder(){
        return this.getCenterY() - getRadiusY() <= cellOccupied.getTopNeighbor().getY() + cellOccupied.getSize() && cellOccupied.getTopNeighbor().isBorder();
    }

    private boolean hitRightBorder(){
        return this.getCenterX() + getRadiusX() >= cellOccupied.getRightNeighbor().getX() && cellOccupied.getRightNeighbor().isBorder();
    }

    private boolean hitLeftBorder(){
        return this.getCenterX() - getRadiusX() <= cellOccupied.getLeftNeighbor().getX() + cellOccupied.getSize() && cellOccupied.getLeftNeighbor().isBorder();
    }

    private boolean movedToCellAbove(){
        return this.getCenterY() - getRadiusY() < cellOccupied.getTopNeighbor().getY() + cellOccupied.getSize();
    }

    private boolean movedToCellBelow(){
        return this.getCenterY() + getRadiusY() > cellOccupied.getBottomNeighbor().getY();
    }

    private boolean movedToCellRight(){
        return this.getCenterX() + getRadiusX() > cellOccupied.getRightNeighbor().getX();
    }

    private boolean movedToCellLeft(){
        return this.getCenterX() - getRadiusX() < cellOccupied.getLeftNeighbor().getX() + cellOccupied.getSize();
    }
    //===========================================================================================================

    //===========================================PORTAL MOVEMENT METHODS==========================================
    private boolean pastBottomCell(){
        return cellOccupied.getY() != OFFSET_ZERO && (super.getCenterY() - getRadiusY()) > cellOccupied.getY() + cellOccupied.getSize();
    }

    private boolean pastTopCell(){
        return cellOccupied.getY() == OFFSET_ZERO && super.getCenterY() - super.getRadiusY() < OFFSET_ZERO; //didn't let whole body disappear because of top row stats.
    }

    private boolean pastFarRightCell(){
        return (super.getCenterX() - getRadiusX()) >= cellOccupied.getX() + cellOccupied.getSize();//unlike other movements, i'm going to make sure the whole body is out of the frame before moving.
    }

    private boolean pastFarLeftCell(){
        return cellOccupied.getX() == 0 && super.getCenterX() + super.getRadiusX() < 0;
    }

    private void moveRightFromPortal() {
        if(cellOccupied.isRightEdgePortal() && pastFarRightCell()){
            teleportRightToLeft();
        }else if(cellOccupied.isLeftEdgePortal() && movedToCellRight()){
            updateMapPosition(cellOccupied.getRightNeighbor());
        }
    }

    private void moveDownFromPortal(){
        if(cellOccupied.isBottomEdgePortal() && pastBottomCell()){
            teleportBottomToTop();
        }else if(cellOccupied.isTopEdgePortal() && movedToCellBelow()){
            updateMapPosition(cellOccupied.getBottomNeighbor());
        }
    }

    private void moveLeftFromPortal(){
        if(cellOccupied.isLeftEdgePortal() && pastFarLeftCell()){
            teleportLeftToRight();
        }else if(cellOccupied.isRightEdgePortal() && movedToCellLeft()){
            updateMapPosition(cellOccupied.getLeftNeighbor());
        }
    }

    private void moveUpFromPortal(){
        if(cellOccupied.isTopEdgePortal() && pastTopCell()){
            teleportTopToBottom();
        }else if(cellOccupied.isBottomEdgePortal() && movedToCellAbove()){
            updateMapPosition(cellOccupied.getTopNeighbor());
        }
    }

    private void teleportLeftToRight(){
        super.setCenterX(cellOccupied.getLeftNeighbor().getX() + cellOccupied.getSize() + super.getRadiusX());
        updateMapPosition(cellOccupied.getLeftNeighbor());
    }

    private void teleportRightToLeft(){
        super.setCenterX(cellOccupied.getRightNeighbor().getX() - super.getRadiusX());
        updateMapPosition(cellOccupied.getRightNeighbor());
    }

    private void teleportTopToBottom(){
        super.setCenterY(cellOccupied.getTopNeighbor().getY() + cellOccupied.getSize() + super.getRadiusY());
        updateMapPosition(cellOccupied.getTopNeighbor());
    }

    private void teleportBottomToTop(){
        super.setCenterY(cellOccupied.getBottomNeighbor().getY() - super.getRadiusY()); //didn't let whole body disappear because of top row stats.
        updateMapPosition(cellOccupied.getBottomNeighbor());
    }

    private void updateMapPosition(MapCell cell){
        this.cellOccupied = cell;

    }

    public void resetPosition(){
        updateMapPosition(startingPosition);
        this.setCenterX(startingPosition.getCenterX());
        this.setCenterY(startingPosition.getCenterY());
    }
    //===========================================================================================================

    //========================================EAT FOOD METHODS===================================================
    public boolean foodInReach(String direction){
        MapCell.Food tempFood = cellOccupied.getCellFood();
        switch(direction){
            case UP:
                return (getCenterY() - getRadiusY() <= tempFood.getCenterY() + tempFood.getRadius());
            case DOWN:
                return (getCenterY() + getRadiusY() >= tempFood.getCenterY());
            case RIGHT:
                return (getCenterX() + getRadiusX() >= tempFood.getCenterX());
            case LEFT:
                return (getCenterX() - getRadiusX() <= tempFood.getCenterX() + tempFood.getRadius());
            default:
                return false;
        }
    }

    public void eatFood(MapCell mapCell){
        if(cellOccupied.isFoodAvailable()){
            mapCell.removeCellFood();
            if(cellOccupied.isBoosterCell()){
                invincible = true;
            }
//            soundEffects.play("munch.wav");
            Statistics.increaseScore(10);
        }
    }
    //====================================================================================================

    //========================================ANIMATION===================================================
    private void mouthAction(String Direction){
        if(closing){
            if (arcLength + MOUTH_SPEED >= 360){
                closing = false;
            }else {
                arcLength += MOUTH_SPEED;
            }
        }else{
            if(arcLength - MOUTH_SPEED <= 270){
                closing = true;
            }else{
                arcLength -= MOUTH_SPEED;
            }
        }
        switch(Direction){
            case UP:
                startAngle = ANGLE_UP + ((arcLength - MOUTH_OPEN) / 2);
                break;
            case DOWN:
                startAngle = ANGLE_DOWN + ((arcLength - MOUTH_OPEN) / 2);
                break;
            case RIGHT:
                startAngle = ANGLE_RIGHT + ((arcLength - MOUTH_OPEN) / 2);
                break;
            case LEFT:
                startAngle = ANGLE_LEFT + ((arcLength - MOUTH_OPEN) / 2);
                break;
        }
        setStartAngle(startAngle);
        setLength(-arcLength);
    }
    //====================================================================================================

    //================================GETTERS FOR OBJECTS AND STATISTICS==================================

    public boolean isInvincible(){
        return invincible;
    }

    public void resetInvicibility(){
        invincible = false;
    }

    public void setInvisible(){
        this.setFill(Color.BLACK);
    }

    public void setVisible(){
        this.setFill(Color.YELLOW);
    }

    public void die(){


        setStartAngle(startAngle);
        setLength(-arcLength);
        if(!(arcLength <=0)){
            arcLength-=4;
            startAngle-=2;
        }

    }

    public void restore(){
        closing = true;
        arcLength = MOUTH_OPEN;
        startAngle = ANGLE_UP;
        setLength(-arcLength);
        setStartAngle(startAngle);
    }
}
