package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * Represents a message transmitting the in flight speed of an airplane
 *
 * @author Andrei Pana 361249
 * @author David Fota 355816
 */

public record AirborneVelocityMessage(long timeStampNs,
                                      IcaoAddress icaoAddress,
                                      double speed, double trackOrHeading) implements Message {

    private static final int ST_START = 48, ST_LENGTH = 3;
    private static final int ST_BITS_START = 21, ST_BITS_LENGTH = 22;
    private static final int DEW_START = 21, DEW_LENGTH = 1;
    private static final int VEW_START = 11, VEW_LENGTH = 10;
    private static final int DNS_START = 10, DNS_LENGTH = 1;
    private static final int VNS_START = 0, VNS_LENGTH = 10;
    private static final int SH_START = 21, SH_LENGTH = 1;
    private static final int HDG_START = 11, HDG_LENGTH = 10;
    private static final int AS_START = 0, AS_LENGTH = 10;


    /**
     * Compact constructor
     *
     * @throws NullPointerException if icaoAddress is null
     * @throws IllegalArgumentException if timeStampNs, speed or trackOrHeading are strictly negative.
     */

    public AirborneVelocityMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs >= 0);
        Preconditions.checkArgument(speed >= 0);
        Preconditions.checkArgument(trackOrHeading >= 0);
    }

    /**
     * Makes it possible to construct a flight speed message from a raw message
     *
     * @param rawMessage
     *          the raw message
     * @return the flight speed message corresponding to the given raw message,
     *                  or null if the subtype is invalid, or if the speed or direction of travel cannot be determined
     */

    public static AirborneVelocityMessage of(RawMessage rawMessage) {
        long payload = rawMessage.payload();
        int st = Bits.extractUInt(payload, ST_START, ST_LENGTH);
        int stBits = Bits.extractUInt(payload, ST_BITS_START, ST_BITS_LENGTH);
        double speed;
        double trackOrHeading;

        if (st < 1 || st > 4) {
            return null;
        }

        switch (st) {
            case 1, 2 -> {
                int vns = Bits.extractUInt(stBits, VNS_START, VNS_LENGTH);
                int dns = Bits.extractUInt(stBits, DNS_START, DNS_LENGTH);
                int vew = Bits.extractUInt(stBits, VEW_START, VEW_LENGTH);
                int dew = Bits.extractUInt(stBits, DEW_START, DEW_LENGTH);
                if (vns == 0 || vew == 0) {
                    return null;
                }
                vns = (dns == 0) ? (--vns) : (--vns) * (-1);
                vew = (dew == 0) ? (--vew) : (--vew) * (-1);
                speed = vectorsToSpeed(st, vns, vew);
                trackOrHeading = getTrack(vns, vew);
                return new AirborneVelocityMessage(rawMessage.timeStampNs(),
                        rawMessage.icaoAddress(),
                        speed,
                        trackOrHeading);
            }
            case 3, 4 -> {
                int as = Bits.extractUInt(stBits, AS_START, AS_LENGTH);
                int hdg = Bits.extractUInt(stBits, HDG_START, HDG_LENGTH);
                int sh = Bits.extractUInt(stBits, SH_START, SH_LENGTH);
                if (sh == 0 || as == 0) {
                    return null;
                }
                as--;
                speed = asToSpeed(st, as);
                trackOrHeading = getHeading(hdg);
                return new AirborneVelocityMessage(rawMessage.timeStampNs(),
                        rawMessage.icaoAddress(),
                        speed,
                        trackOrHeading);
            }
        }
        return null;
    }


    private static double getHeading(int hdg) {
        double heading = hdg / Math.scalb(1, 10);
        heading = Units.convertFrom(heading, Units.Angle.TURN);
        return heading;
    }

    private static double getTrack(int vns, int vew) {
        double track = Math.atan2(vew, vns);
        track = (track < 0) ? track + Math.PI * 2 : track;
        return track;
    }

    private static double vectorsToSpeed(int st, int vns, int vew) {
        double speed = Math.sqrt(Math.pow(vns, 2) + Math.pow(vew, 2));
        speed = (st == 1) ? Units.convertFrom(speed, Units.Speed.KNOT) :
                Units.convertFrom(4 * speed, Units.Speed.KNOT);
        return speed;
    }

    private static double asToSpeed(int st, int as) {
        return (st == 3) ? Units.convertFrom(as, Units.Speed.KNOT) :
                Units.convertFrom(4 * as, Units.Speed.KNOT);
    }


}
