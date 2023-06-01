package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

import java.io.IOException;

/**
 * Represents a final class that controls the map's functionality.
 * @author David Fota 355816
 * @author Andrei Pana 361249
 */
public final class BaseMapController {
    /**
     * Number Of Pixels in a map tile
     */
    private static final int PIXELS_IN_TILE = 256;

    private final TileManager tileManager;
    private final MapParameters mapParameters;
    private  final Canvas canvas;
    private final Pane pane;
    private boolean redrawNeeded;
    private Point2D dragInitial;

    /**
     * Constructs a public BaseMapController object.
     * Initializes the canvas and pane, binds their size properties,
     * adds listeners for zoom, minX, and minY properties, and adds mouse actions.
     *
     * @param tileManager   The object for managing map tiles.
     * @param mapParameters The object containing map parameters.
     */
    public BaseMapController(TileManager tileManager, MapParameters mapParameters) {
        this.tileManager = tileManager;
        this.mapParameters = mapParameters;

        canvas = new Canvas();
        pane = new Pane(canvas);
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        addAllListeners(mapParameters.zoomProperty(), mapParameters.minXProperty(), mapParameters.minYProperty());
        addAllMouseActions();
    }

    /**
     * Returns the Pane object containing the map.
     *
     * @return The Pane object.
     */
    public Pane pane() {
        return pane;
    }


    /**
     * Centers the map view on the specified geographical position.
     *
     * @param point The geographical position to center the map on.
     */
    public void centerOn(GeoPos point) {
        int zoomLvl = mapParameters.getZoomValue();
        double x = WebMercator.x(zoomLvl, point.longitude()) - mapParameters.getMinXValue() - (canvas.getWidth() / 2);
        double y = WebMercator.y(zoomLvl, point.latitude()) - mapParameters.getMinYValue() - (canvas.getHeight() / 2);
        mapParameters.scroll(x, y);
    }

    /**
     * Checks if redrawing the map is needed, and if so, triggers one on the next pulse.
     */
    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;
        drawMap(pane);
    }

    /**
     * Sets the flag to indicate redrawing the map is needed and requests it at the next pulse.
     */
    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }


    /**
     * Draws the map on the given pane.
     *
     * @param pane The Pane object to draw the map on.
     */
    private void drawMap(Pane pane) {
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.clearRect(0,0, pane.getWidth(), pane.getHeight());

        double minX = mapParameters.getMinXValue();
        double minY = mapParameters.getMinYValue();
        int firstTileX = (int) minX / PIXELS_IN_TILE;
        int firstTileY = (int) minY / PIXELS_IN_TILE;


        // added 1 for smoother drag transition
        for (int i = firstTileX; i <= firstTileX + (pane.getWidth() / PIXELS_IN_TILE) + 1; i++) {
            for (int j = firstTileY; j <= firstTileY + (pane.getHeight() / PIXELS_IN_TILE) + 1; j++) {
                TileManager.TileId tile = new TileManager.TileId(mapParameters.getZoomValue(), i, j);
                if(tile.isValid()){
                    try {
                        graphicsContext.drawImage(
                                tileManager.imageForTileAt(tile)
                                , i * PIXELS_IN_TILE - minX
                                , j * PIXELS_IN_TILE - minY);
                    } catch (IOException ignored) {}
                }
            }
        }
    }

    /**
     * Adds listeners for zoom, minX, and minY properties and their changes.
     *
     * @param zoom The ReadOnlyIntegerProperty for zoom level.
     * @param minX The ReadOnlyDoubleProperty for minimum X value.
     * @param minY The ReadOnlyDoubleProperty for minimum Y value.
     */
    private void addAllListeners(ReadOnlyIntegerProperty zoom, ReadOnlyDoubleProperty minX, ReadOnlyDoubleProperty minY)
    {
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });
        zoom.addListener((o, oV, nV) -> redrawOnNextPulse());
        minX.addListener((o, oV, nV) -> redrawOnNextPulse());
        minY.addListener((o, oV, nV) -> redrawOnNextPulse());
        canvas.widthProperty().addListener((o, oV, nV) -> redrawOnNextPulse());
        canvas.heightProperty().addListener((o, oV, nV) -> redrawOnNextPulse());
    }

    /**
     * Adds mouse actions for dragging and scrolling on the map pane.
     */
    private void addAllMouseActions() {

        pane.setOnMousePressed(e -> dragInitial = new Point2D(e.getX(),e.getY()));


        pane.setOnMouseDragged(e -> {
            double presentX = e.getX();
            double presentY = e.getY();
            mapParameters.scroll( dragInitial.getX() - presentX, dragInitial.getY() - presentY);
            dragInitial = new Point2D(presentX, presentY);
        });

        pane.setOnMouseReleased(e -> dragInitial = null);

        LongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {
            int zoomDelta = (int) Math.signum(e.getDeltaY());
            if (zoomDelta == 0) return;

            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);

            //makes sure the zoom is centered on the mouse cursor
            double xTranslation = e.getX();
            double yTranslation = e.getY();
            mapParameters.scroll(xTranslation, yTranslation);
            mapParameters.changeZoomLevel(zoomDelta);
            mapParameters.scroll(-xTranslation, -yTranslation);
        });
    }
}
