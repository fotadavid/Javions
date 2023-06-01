package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

/**
 * Represents an "aircraft state accumulator",
 * i.e. an object accumulating ADS-B messages originating from a single aircraft
 * in order to determine its state over time
 *
 * @author Andrei Pana 361249
 * @author David Fota 355816
 */

public class AircraftStateAccumulator<T extends AircraftStateSetter> {
    final T stateSetter;
    private final static long NANO_IN_NORMAL= (long) Math.pow(10, 9);
    private static final int EVEN = 0, ODD = 1;
    private AirbornePositionMessage lastEvenMessage, lastOddMessage;

    /**
     * Public constructor
     * @param stateSetter
     *
     * @return an aircraft state accumulator associated with the given modifiable state
     * @throws NullPointerException if it is null.
     */
    public AircraftStateAccumulator(T stateSetter) {
        this.stateSetter = stateSetter;
        if (stateSetter == null)
            throw new NullPointerException();
    }

    /**
     * @return the modifiable state of the aircraft passed to its constructor
     */

    public T stateSetter() {
        return stateSetter;
    }

    /**
     * updates the editable status according to the given message
     * @param message
     *      the message
     */

    public void update(Message message) {
        GeoPos position;
        stateSetter.setLastMessageTimeStampNs(message.timeStampNs());
        switch (message) {
            case AircraftIdentificationMessage aim -> {
                stateSetter.setCategory(aim.category());
                stateSetter.setCallSign(aim.callSign());
            }
            case AirbornePositionMessage apm -> {
                stateSetter.setAltitude(apm.altitude());
                if (apm.parity() == EVEN)
                    lastEvenMessage = apm;
                else
                    lastOddMessage = apm;
                if(lastOddMessage != null && lastEvenMessage != null && isPositionValid()) {
                    position = CprDecoder.decodePosition(lastEvenMessage.x(),
                            lastEvenMessage.y(), lastOddMessage.x(), lastOddMessage.y(), apm.parity());
                    if (position != null) stateSetter.setPosition(position);
                }
            }
            case AirborneVelocityMessage avm -> {
                stateSetter.setVelocity(avm.speed());
                stateSetter.setTrackOrHeading(avm.trackOrHeading());
            }
            default -> throw new Error();
        }
    }
    private boolean isPositionValid(){
        return Math.abs(lastOddMessage.timeStampNs() - lastEvenMessage.timeStampNs()) <= 10 * NANO_IN_NORMAL;
    }
}
