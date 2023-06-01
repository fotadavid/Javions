package ch.epfl.javions;

/**
 * contains (static) methods to project geographical coordinates
 * according to the WebMercator projection
 *
 * @author Andrei Pana 361249
 * @author David Fota 355816
 */

public final class WebMercator {
    private WebMercator() {
    }

    /**
     * @param zoomLevel given zoom level
     * @param longitude the longitude
     * @return the x coordinate corresponding to the given longitude
     * (in radians) at the given zoom level
     */
    public static double x(int zoomLevel, double longitude) {
        return (Math.scalb(1, 8 + zoomLevel)) * (Units.convertTo(longitude, Units.Angle.TURN) + 0.5);
    }

    /**
     * @param zoomLevel given zoom level
     * @param latitude  the latitude
     * @return the y coordinate corresponding to the given latitude
     * (in radians) at the given zoom level
     */
    public static double y(int zoomLevel, double latitude) {
        return (Math.scalb(1, 8 + zoomLevel)) * ((-((Math2.asinh(Math.tan(latitude))) / (2 * Math.PI))) + 0.5);
    }
}
