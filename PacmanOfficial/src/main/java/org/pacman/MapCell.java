package org.pacman;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MapCell extends Rectangle{

    private static int mapFoodLeft;
    private final Color color;

    //TYPE OF MAP-CELL
    private final boolean border;
    private final boolean portal;
    private boolean isLeftEdgePortal;
    private boolean isRightEdgePortal;
    private boolean isBottomEdgePortal;
    private boolean isTopEdgePortal;
    private final List<Rectangle> cellBorders;
    private final double borderSize;

    //POINTERS TO NEIGHBORS & starting positions
    private static MapCell ghostStart;
    private static MapCell pacmanStart;
    private boolean isBoosterCell;
    private MapCell leftNeighbor;
    private MapCell rightNeighbor;
    private MapCell topNeighbor;
    private MapCell bottomNeighbor;
    private final double cellMidX;
    private final double cellMidY;

    //INNER CLASS INSTANCE
    private Food cellFood = null; //-->ONLY CREATED IF NOT A BORDER CELL

    //CONSTRUCTOR
    public MapCell(double xPos, double yPos, double size, boolean border, boolean portal, Color color){
        super(xPos, yPos, size, size);
        cellBorders = new ArrayList<>();
        this.border = border;
        this.portal = portal;
        this.isBoosterCell = false;
        this.color = color;
        cellMidX = (xPos + (xPos + size)) / 2; //used for determining location of food and for movement of pacman.
        cellMidY = (yPos + (yPos + size)) / 2;
        if(!border){
            cellFood = new Food(cellMidX, cellMidY, size);
        }
        borderSize = size / 10;
    }

    private MapCell(double x, double y){
        this(x, y, 0, false, false, null);
    }

    public static MapCell cellFactory(double x, double y, double size){
        /*method is for obtaining a coordinateArea that contains the single point passed to it (so it would need
            to be divisible by a cell size.*/
        x = (x % size != 0) ? (x / size) * size : x; //returns an x divisible by size.

        y = (y % size != 0) ? (y / size) * size : y; //returns a y divisible by size.

        return new MapCell(x, y);
    }

    private MapCell getCellContainingCoordinates(double x, double y){
        return cellFactory(x, y, getSize());
    }

    //======================================CLASS STATES==============================================
    public boolean isBorder(){
        return border;
    }

    public boolean isPortal(){
        return portal;
    }

    public boolean isBoosterCell(){
        return isBoosterCell;
    };

    public boolean isFoodAvailable(){
        return cellFood.isFoodAvailable();
    }
    //================================================================================================

    //======================================GETTERS===================================================
    public double getCenterX(){return cellMidX;}

    public double getCenterY(){return cellMidY;}

    public static int getMapFoodLeft(){
        return mapFoodLeft;
    }

    public double getSize(){
        return super.getHeight();
    }

    public boolean isRightEdgePortal(){
        return isRightEdgePortal;
    }

    public boolean isLeftEdgePortal(){
        return isLeftEdgePortal;
    }

    public boolean isBottomEdgePortal(){
        return isBottomEdgePortal;
    }

    public boolean isTopEdgePortal(){
        return isTopEdgePortal;
    }

    //used for linking all cells in the map.
    public MapCell getLeftNeighbor() { return leftNeighbor; }

    public MapCell getRightNeighbor() { return rightNeighbor; }

    public MapCell getTopNeighbor() { return topNeighbor; }

    public MapCell getBottomNeighbor() { return bottomNeighbor; }

    public MapCell getPotentialLeftNeighbor(){
        return getCellContainingCoordinates(getX() - getSize(), getY());
    }

    public MapCell getPotentialRightNeighbor(){
        return getCellContainingCoordinates(getX() + getSize(), getY());
    }

    public MapCell getPotentialTopNeighbor(){
        return getCellContainingCoordinates(getX(), getY() - getSize());
    }

    public MapCell getPotentialBottomNeighbor(){
        return getCellContainingCoordinates(getX(), getY() + getSize());
    }

    public List<Rectangle> getBorders(){
        return this.cellBorders;
    }

    public Food getCellFood(){return this.cellFood;}

    @Override
    public boolean equals(Object obj) {
        double threshold = .00001;
        if(obj instanceof MapCell)
            return (Math.abs(((MapCell) obj).getX() - getX()) <= threshold && Math.abs( ((MapCell) obj).getY() - getY()) <= threshold);
        else
            return false;
    }
    //================================================================================================

    //======================================SETTERS===================================================

    public void setGhostStart(){
        ghostStart = this;
    }

    public void setPacmanStart(){
        pacmanStart = this;
    }

    public void setBoosterCell(){
        this.getCellFood().makeBoosterFood();
        this.isBoosterCell = true;
    }

    public void setCellNeighbors(double[] mapDimensions, LinkedList<MapCell> cellMap){ //sets the neighbors based on this cell's info
        double firstXPosition = 0;
        double firstYPosition = getSize() * 2;
        double lastXPosition = mapDimensions[0];
        double lastYPosition = mapDimensions[1];
        setLeftNeighbor(cellMap, getPotentialLeftNeighbor());
        setRightNeighbor(cellMap, getPotentialRightNeighbor());
        setTopNeighbor(cellMap, getPotentialTopNeighbor());
        setBottomNeighbor(cellMap, getPotentialBottomNeighbor());
        if(isPortal()){
            if(getX() == firstXPosition){
                isLeftEdgePortal = true;
                setLeftNeighbor(cellMap, getCellContainingCoordinates(lastXPosition, getY()));
            }else if(getX() == lastXPosition){
                isRightEdgePortal = true;
                setRightNeighbor(cellMap, getCellContainingCoordinates(firstXPosition, getY()));
            }else if(getY() == lastYPosition){
                isBottomEdgePortal = true;
                setBottomNeighbor(cellMap, getCellContainingCoordinates(getX(), firstYPosition));
            }else{
                isTopEdgePortal = true;
                setTopNeighbor(cellMap, getCellContainingCoordinates(getX(), lastYPosition));
            }
        }

        if(isBorder()){
            drawBorders();
        }
    }

    public void setLeftNeighbor(LinkedList<MapCell> map, MapCell neighborCell){
        if (map.contains(neighborCell)){
            this.leftNeighbor = map.get(map.indexOf(neighborCell));
        }
    }

    public void setRightNeighbor(LinkedList<MapCell> map, MapCell neighborCell) {
        if (map.contains(neighborCell)) {
            this.rightNeighbor = map.get(map.indexOf(neighborCell));
        }
    }

    public void setTopNeighbor(LinkedList<MapCell> map, MapCell neighborCell) {
        if (map.contains(neighborCell)) {
            this.topNeighbor = map.get(map.indexOf(neighborCell));
        }
    }

    public void setBottomNeighbor(LinkedList<MapCell> map, MapCell neighborCell) {
        if (map.contains(neighborCell)) {
            this.bottomNeighbor = map.get(map.indexOf(neighborCell));
        }
    }

    public void drawBorders() {
        if(rightNeighbor == null || !rightNeighbor.isBorder()) {
            drawRightBorder();
        }
        if(leftNeighbor == null || !leftNeighbor.isBorder()) {
            drawLeftBorder();
        }
        if(bottomNeighbor == null || !bottomNeighbor.isBorder()) {
            drawBottomBorder();
            if(rightNeighbor != null && rightNeighbor.isBorder()){
                drawBottomRightCorner();
            }
            if(leftNeighbor != null && leftNeighbor.isBorder()){
                drawBottomLeftCorner();
            }
        }
        if(topNeighbor == null || !topNeighbor.isBorder()) {
            drawTopBorder();
            if(rightNeighbor != null && rightNeighbor.isBorder()){
                drawTopRightCorner();
            }
            if(leftNeighbor != null && leftNeighbor.isBorder()){
                drawTopLeftCorner();
            }
        }
    }

    public void drawTopRightCorner(){
        double startingX = getRightNeighbor().getX();
        double startingY = getY();
        Rectangle rectangle = new Rectangle(startingX, startingY, borderSize, borderSize);
        rectangle.setFill(color);
        cellBorders.add(rectangle);
    }

    public void drawTopLeftCorner(){
        double startingX = getLeftNeighbor().getX() + getSize() - (borderSize);
        double startingY = getY();
        Rectangle rectangle = new Rectangle(startingX, startingY, borderSize, borderSize);
        rectangle.setFill(color);
        cellBorders.add(rectangle);
    }

    public void drawBottomRightCorner(){
        double startingX = getRightNeighbor().getX();
        double startingY = getY() + getSize() - (borderSize);
        Rectangle rectangle = new Rectangle(startingX, startingY, borderSize, borderSize);
        rectangle.setFill(color);
        cellBorders.add(rectangle);
    }

    public void drawBottomLeftCorner(){
        double startingX = getLeftNeighbor().getX() + getSize() - (borderSize);
        double startingY = getY() + getSize() - (borderSize);
        Rectangle rectangle = new Rectangle(startingX, startingY, borderSize, borderSize);
        rectangle.setFill(color);
        cellBorders.add(rectangle);
    }

    public void drawRightBorder(){
        double startingX = getX() + getSize() - (borderSize);
        Rectangle rectangle = new Rectangle(startingX, getY(), borderSize, getSize());
        rectangle.setFill(color);
        cellBorders.add(rectangle);
    }

    public void drawLeftBorder(){
        double startingX = getX();
        Rectangle rectangle =new Rectangle(startingX, getY(), borderSize, getSize());
        rectangle.setFill(color);
        cellBorders.add(rectangle);
    }

    public void drawBottomBorder(){
        double startingY = getY() + getSize() - (borderSize);
        Rectangle rectangle = new Rectangle(getX(), startingY, getSize(), borderSize);
        rectangle.setFill(color);
        cellBorders.add(rectangle);
    }

    public void drawTopBorder(){
        double startingY = getY();
        Rectangle rectangle = new Rectangle(getX(), startingY, getSize(), borderSize);
        rectangle.setFill(color);
        cellBorders.add(rectangle);
    }

    public void removeCellFood(){
        cellFood.removeCellFood();
        mapFoodLeft -= 1;
    }

    public static void resetFood(){
        mapFoodLeft = 0;
    }

    public static void increaseMapFood(){
        mapFoodLeft++;
    } //used when initializing the main game map to count how much food is left in the map.



    //================================================================================================

    //======================================INNER CLASSES FOOD========================================
    public static final class Food extends Circle {

        private boolean foodAvailable = true;

        private Food(double x, double y, double radius){
            super (x, y, radius * .04);
            super.setFill(Color.TAN);
        }

        public void makeBoosterFood(){
            super.setRadius(getRadius() * 5);
            super.setFill(Color.YELLOW);
        }

        public boolean isFoodAvailable(){
            return foodAvailable;
        }

        public void removeCellFood(){
            foodAvailable = false;
            super.setFill(null);
        }
    }
    //================================================================================================
}
