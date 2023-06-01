package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;

/**
 * represents a decoder of the CPR position
 *
 * @author Andrei Pana 361249
 * @author David Fota 355816
 */


public final class CprDecoder {
    private static final double DELTA0 = 1d / 60;
    private static final double DELTA1 = 1d / 59;
    private static final int LATITUDE_ZONES_0 = 60;
    private static final int LATITUDE_ZONES_1 = 59;
    /**
     * Decodes the position of an aircraft from two CPR messages.
     *
     * @param x0 and y0 being the local longitude and latitude of an even message,
     * @param x1 and y1 those of an odd message — knowing that the most recent positions are those of
     * mostRecent index (0 or 1);
     * @return null if the latitude of the decoded position is invalid (i.e. between ±90°)
     * or if the position cannot be determined due to a change in latitude band or the decoded positions
     * @throws IllegalArgumentException if mostRecent is not 0 or 1.
     */
    public static GeoPos decodePosition(double x0, double y0, double x1, double y1, int mostRecent) {
        Preconditions.checkArgument(mostRecent == 0 || mostRecent == 1);
        int zLat0, zLat1, zLong0, zLong1;
        int zLat = (int) Math.rint(y0 * LATITUDE_ZONES_1 - y1 * LATITUDE_ZONES_0);
        if (zLat < 0) {
            zLat0 = zLat + LATITUDE_ZONES_0;
            zLat1 = zLat + LATITUDE_ZONES_1;
        } else {
            zLat0 = zLat;
            zLat1 = zLat;
        }
        double lat0, lat1;
        lat0 = computeLatOrLong(zLat0, y0, DELTA0);
        lat1 = computeLatOrLong(zLat1, y1, DELTA1);
        double a = computeA(lat0);
        if (checkIfBandChanged(a, computeA(lat1)) && !Double.isNaN(a))
            return null;
        if (Double.isNaN(a)) {
            zLong0 = 1;
            zLong1 = 1;
        } else {
            zLong0 = (int) Math.floor(2 * Math.PI / a);
            zLong1 = zLong0 - 1;
        }
        int zLong = (int) Math.rint(x0 * zLong1 - x1 * zLong0);
        double long0 = computeLatOrLong(zLong, x0, 1d / zLong0);
        double long1 = computeLatOrLong(zLong, x1, 1d / zLong1);
        if (mostRecent == 0) {
            if (isLatValid(lat0)) {
                return createGeoPos(long0, lat0);
            } else {
                return null;
            }
        } else {
            if (isLatValid(lat1)) {
                return createGeoPos(long1, lat1);
            } else {
                return null;
            }
        }
    }

    /**
     * Computes the latitude or longitude
     * @param zLat latitude zone
     * @param y latitude
     * @param delta delta
     * @return the latitude
     */
    private static double computeLatOrLong(int zLat, double y, double delta){
        double lat = delta * (zLat + y);
        lat = checkLongOrLat(lat);
        return lat;
    }

    /**
     * Checks if the latitude is valid
     * @param lat latitude
     * @return true if the latitude is valid
     */
    private static boolean isLatValid(double lat) {
        return GeoPos.isValidLatitudeT32((int) Math.rint(Units.convert(lat, Units.Angle.TURN, Units.Angle.T32)));
    }

    /**
     * Checks if the longitude or latitude is valid
     * @param a computed in decodePosition
     * @return the value of a after modification
     */
    private static double checkLongOrLat(double a) {
        if (a >= 0.5)
            a--;
        return a;
    }

    /**
     * Creates a GeoPos
     * @param a x coordinate
     * @param b y coordinate
     * @return the GeoPos
     */
    private static GeoPos createGeoPos(double a, double b) {
        return new GeoPos((int) Math.rint(Units.convert(a, Units.Angle.TURN, Units.Angle.T32)),
                (int) Math.rint(Units.convert(b, Units.Angle.TURN, Units.Angle.T32)));
    }

    /**
     * Computes the value of a
     * @param lat0 latitude
     * @return the value of a
     */
    private static double computeA(double lat0) {
        return Math.acos(1 - ((1 - Math.cos(2 * Math.PI * DELTA0)) /
                (Math.cos(Units.convertFrom(lat0, Units.Angle.TURN)) *
                        Math.cos(Units.convertFrom(lat0, Units.Angle.TURN)))));
    }

    /**
     * Checks if the band changed
     * @param a1 value of a computed for first coordinate
     * @param a2 value of a computed for second coordinate
     * @return true if the band changed
     */
    private static boolean checkIfBandChanged(double a1, double a2) {
        return Math.floor((2 * Math.PI) / a1) != Math.floor((2 * Math.PI) / a2);
    }
}
