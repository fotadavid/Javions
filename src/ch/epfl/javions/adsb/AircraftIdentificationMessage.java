package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * represents an ADS-B in-flight identification message
 *
 * @author Andrei Pana 361249
 * @author David Fota 355816
 */
public record AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAdress,
                                            int category, CallSign callSign) implements Message {
    private static final int START_POSITION = 48;
    private static final int CHARACTER_SIZE = 6;
    private static final int TYPE_CODE_SIZE = 3;
    private static final int LETTER_START = 1;
    private static final int LETTER_END = 26;
    private static final int ASCII_LETTER_INDEX = 'A' - 1;
    private static final int ASCII_NUMBER_START = 48;
    private static final int ASCII_NUMBER_END = 57;
    private static final int ASCII_SPACE_INDEX = 32;
    public AircraftIdentificationMessage {
        Preconditions.checkArgument(timeStampNs >= 0);
        if (icaoAdress == null || callSign == null)
            throw new NullPointerException();
    }
    /**
     * method that creates an AircraftIdentificationMessage from a RawMessage
     *
     * @param rawMessage the raw message
     * @returns the in-flight positioning message corresponding to the given raw message, or null if the altitude
     * it contains is invalid
     */
    public static AircraftIdentificationMessage of(RawMessage rawMessage) {
        long me = rawMessage.payload();
        StringBuilder callString = new StringBuilder();
        for (int i = 42; i >= 0; i -= 6) {
            int a = Bits.extractUInt(me, i, CHARACTER_SIZE);
            if (isLetter(a))
                callString.append((char) (a + ASCII_LETTER_INDEX));
            else if (isNumberOrSpace(a))
                callString.append((char) a);
            else return null;
        }
        int category = (14 - rawMessage.typeCode() << 4) | Bits.extractUInt(me, START_POSITION, TYPE_CODE_SIZE);
        return new AircraftIdentificationMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(),
                category, new CallSign(callString.toString().stripTrailing()));
    }

    /**
     * method that returns the category of the aircraft
     * @return the category of the aircraft
     */
    @Override
    public long timeStampNs() {
        return timeStampNs;
    }

    /**
     * method that returns the category of the aircraft
     * @return the category of the aircraft
     */
    @Override
    public IcaoAddress icaoAddress() {
        return icaoAdress;
    }

    /**
     * returns true if the value corresponds to a letter according to the rules given
     * in the project statement
     * @param a the value
     * @return true if the value corresponds to a letter according to the rules given
     */
    private static boolean isLetter(int a){return a >= LETTER_START && a <= LETTER_END;}

    /**
     * returns true if the value corresponds to a number or a space according to the rules given
     * in the project statement
     * @param a the value
     * @return true if the value corresponds to a number or a space according to the rules givenÒ
     */
    private static boolean isNumberOrSpace(int a){
        return (ASCII_NUMBER_START <= a && a <= ASCII_NUMBER_END) || a == ASCII_SPACE_INDEX;
    }
}
