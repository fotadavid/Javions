package ch.epfl.javions.adsb;

/**
 * Transforms the raw ADS-B messages into messages of one of the three types described below:
 * — AircraftIdentificationMessage, AirbornePositionMessage or AirborneVelocityMessage
 *
 * @author Andrei Pana 361249
 * @author David Fota 355816
 */

public class MessageParser {

    private final static int AIRCRAFT_IDENTIFICATION_TYPECODE_START = 1, AIRCRAFT_IDENTIFICATION_TYPECODE_END = 4;
    private final static int FIRST_AIRBORNE_POSITION_TYPECODE_START = 9, FIRST_AIRBORNE_POSITION_TYPECODE_END = 18;
    private final static int SECOND_AIRBORNE_POSITION_TYPECODE_START = 20, SECOND_AIRBORNE_POSITION_TYPECODE_END = 22;
    private final static int AIRBORNE_VELOCITY_TYPECODE = 19;

    private MessageParser(){}

    /**
     * Transforms a raw message into the corresponding message
     *
     * @param rawMessage the raw message
     * @return the instance of AircraftIdentificationMessage, AirbornePositionMessage or AirborneVelocityMessage
     * corresponding to the given raw message, or null if the type code of the latter does not correspond to
     * any of these three types of messages, or if it is invalid
     */
    public static Message parse(RawMessage rawMessage) {
        int typecode = rawMessage.typeCode();

        if (typecode >= AIRCRAFT_IDENTIFICATION_TYPECODE_START
                && typecode <= AIRCRAFT_IDENTIFICATION_TYPECODE_END) {
            return AircraftIdentificationMessage.of(rawMessage);

        } else if ((typecode >= FIRST_AIRBORNE_POSITION_TYPECODE_START
                && typecode <= FIRST_AIRBORNE_POSITION_TYPECODE_END)
                || (typecode >= SECOND_AIRBORNE_POSITION_TYPECODE_START
                && typecode <= SECOND_AIRBORNE_POSITION_TYPECODE_END)) {
            return AirbornePositionMessage.of(rawMessage);

        } else if (typecode == AIRBORNE_VELOCITY_TYPECODE) {
            return AirborneVelocityMessage.of(rawMessage);
        }

        return null;
    }
}
