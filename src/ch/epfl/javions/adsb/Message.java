package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * The Message interface represents a generic message.
 * @author Andrei Pana 361249
 * @author David Fota 355816
 */
public interface Message {

    /**
     * Returns the timestamp in nanoseconds of the message.
     *
     * @return the timestamp in nanoseconds
     */
     long timeStampNs();

    /**
     * Returns the ICAO address associated with the message.
     *
     * @return the IcaoAddress object representing the ICAO address
     */
     IcaoAddress icaoAddress();
}