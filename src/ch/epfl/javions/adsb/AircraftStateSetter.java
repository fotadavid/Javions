package ch.epfl.javions.adsb;
import ch.epfl.javions.GeoPos;

/**
 * The AircraftStateSetter interface defines methods for setting various properties of an aircraft state.
 * @author Andrei Pana 361249
 *  * @author David Fota 355816
 */
public interface AircraftStateSetter {

    /**
     * Sets the timestamp in nanoseconds of the last message received from the aircraft.
     *
     * @param timeStampNs the timestamp in nanoseconds
     */
    void setLastMessageTimeStampNs(long timeStampNs);

    /**
     * Sets the category of the aircraft.
     *
     * @param category the category value
     */
    void setCategory(int category);

    /**
     * Sets the call sign of the aircraft.
     *
     * @param callSign the CallSign object representing the call sign
     */
    void setCallSign(CallSign callSign);

    /**
     * Sets the position of the aircraft.
     *
     * @param position the GeoPos object representing the position
     */
    void setPosition(GeoPos position);

    /**
     * Sets the altitude of the aircraft.
     *
     * @param altitude the altitude value
     */
    void setAltitude(double altitude);

    /**
     * Sets the velocity of the aircraft.
     *
     * @param velocity the velocity value
     */
    void setVelocity(double velocity);

    /**
     * Sets the track or heading of the aircraft.
     *
     * @param trackOrHeading the track or heading value
     */
    void setTrackOrHeading(double trackOrHeading);
}