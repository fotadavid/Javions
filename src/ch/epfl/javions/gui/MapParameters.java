package ch.epfl.javions.gui;

import ch.epfl.javions.Math2;
import ch.epfl.javions.Preconditions;
import javafx.beans.property.*;

/**
 * Represents a final class that manages the parameters of the map (zoom, minX, minY)
 * @author David Fota 355816
 * @author Andrei Pana 361249
 */
public final class MapParameters {
    private final IntegerProperty zoom;
    private final DoubleProperty minX, minY;
    /**
     * The minimum and maximum zoom values
     */
    private final static int MIN_ZOOM = 6, MAX_ZOOM = 19;

    /**
     * Constructor of the class
     * @param zoom the zoom level
     * @param minX the minimum x coordinate value
     * @param minY the minimum y coordinate value
     */
    public MapParameters(int zoom, double minX, double minY) {
        Preconditions.checkArgument(zoom >= MIN_ZOOM && zoom <= MAX_ZOOM);
        this.zoom = new SimpleIntegerProperty(zoom);
        this.minX = new SimpleDoubleProperty(minX);
        this.minY = new SimpleDoubleProperty(minY);
    }

    /**
     * Getter of the zoom property
     * @return the zoom property
     */
    public ReadOnlyIntegerProperty zoomProperty() {
        return zoom;
    }

    /**
     * Getter of the zoom value
     * @return the zoom value
     */
    public int getZoomValue() {
        return zoom.get();
    }

    /**
     * Getter of the minX property
     * @return the minX property
     */
    public ReadOnlyDoubleProperty minXProperty() {
        return minX;
    }

    /**
     * Getter of the minX value
     * @return the minX value
     */
    public double getMinXValue() {
        return minX.get();
    }

    /**
     * Getter of the minY property
     * @return the minY property
     */
    public ReadOnlyDoubleProperty minYProperty() {
        return minY;
    }

    /**
     * Getter of the minY value
     * @return the minY value
     */
    public double getMinYValue() {
        return minY.get();
    }

    /**
     * Method that scrolls the map
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void scroll(double x, double y) {
        minX.set(minX.get() + x);
        minY.set(minY.get() + y);
    }

    /**
     * Method that changes the zoom level
     * @param zoomDifference the difference between the current zoom level and the new zoom level
     */
    public void changeZoomLevel(int zoomDifference) {
        int previousZoom = zoom.get();
        zoom.set(Math2.clamp(MIN_ZOOM, previousZoom + zoomDifference, MAX_ZOOM));
        zoomDifference = previousZoom - zoom.get();
        adaptTopLeftCorner(zoomDifference);
    }
    /**
     * Method that adapts the top left corner of the map
     * @param zoomDifference the difference between the current zoom level and the new zoom level
     */
    private void adaptTopLeftCorner(int zoomDifference) {
        double var = 1 / Math.pow(2, zoomDifference);
        minX.set(minX.get() * var);
        minY.set(minY.get() * var);
    }
}
