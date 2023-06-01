package ch.epfl.javions;

/**
 * Definition of various units
 *
 * @author Andrei Pana 361249
 * @author David Fota 355816
 */
public final class Units {
    private Units() {
    }

    /**
     * The conversion factor for centi- units (0.01).
     * Used to convert between centimeters and meters.
     */
    public static final double CENTI = 1e-2;

    /**
     * The conversion factor for kilo units (1000).
     * Used to convert between kilograms and grams.
     */
    public static final double KILO = 1e3;

    /**
     * The conversion factor for nano- units (0.000000001).
     * Used to convert between nanoseconds and seconds.
     */
    public static final double NANO = 1e-9;

    /**
     * Angle related units
     */
    public static final class Angle {
        private Angle() {
        }

        /**
         * The main angle unit
         */
        public static final double RADIAN = 1;

        /**
         * The conversion factor for turns (2 * Math.PI * RADIAN).
         * Represents the value of an angle in turns (full circle).
         */
        public static final double TURN = 2 * Math.PI * RADIAN;

        /**
         * The conversion factor for degrees (TURN / 360).
         * Represents the value of an angle in degrees.
         */
        public static final double DEGREE = TURN / 360;

        /**
         * The conversion factor for T32 (TURN / (1L << 32)).
         * Represents the value of an angle in T32 units.
         */
        public static final double T32 = TURN / (1L << 32);
    }

    /**
     * Time related units
     */
    public static final class Time {
        private Time() {
        }

        /**
         * The main time unit used.
         */
        public static final double SECOND = 1;

        /**
         * The conversion factor for minutes (60 * SECOND).
         * Represents the value of time in minutes.
         */
        public static final double MINUTE = 60 * SECOND;

        /**
         * The conversion factor for hours (60 * MINUTE).
         * Represents the value of time in hours.
         */
        public static final double HOUR = 60 * MINUTE;

        /**
         * The conversion factor for nanoseconds (NANO * SECOND).
         * Represents the value of time in nanoseconds.
         */
        public static final double NANOSECOND = NANO * SECOND;
    }


    /**
     * Length related units
     */
    public static final class Length {
        private Length() {
        }

        /**
         * the main length unit.
         */
        public static final double METER = 1;

        /**
         * The conversion factor for centimeters (CENTI * METER).
         * Represents the value of length in centimeters.
         */
        public static final double CENTIMETER = CENTI * METER;

        /**
         * The conversion factor for kilometers (KILO * METER).
         * Represents the value of length in kilometers.
         */
        public static final double KILOMETER = KILO * METER;

        /**
         * The conversion factor for inches (2.54 * CENTIMETER).
         * Represents the value of length in inches.
         */
        public static final double INCH = 2.54 * CENTIMETER;

        /**
         * The conversion factor for feet (12 * INCH).
         * Represents the value of length in feet.
         */
        public static final double FOOT = 12 * INCH;

        /**
         * The conversion factor for nautical miles (1852 * METER).
         * Represents the value of length in nautical miles.
         */
        public static final double NAUTICAL_MILE = 1852 * METER;
    }

    /**
     * Speed related units
     */
    public static final class Speed {
        private Speed() {
        }

        /**
         * The conversion factor for meters per second (Length.METER / Time.SECOND).
         * Represents the value of speed in meters per second.
         */
        public static final double METERS_PER_SECOND = Length.METER / Time.SECOND;

        /**
         * The conversion factor for knots (Length.NAUTICAL_MILE / Time.HOUR).
         * Represents the value of speed in knots.
         */
        public static final double KNOT = Length.NAUTICAL_MILE / Time.HOUR;

        /**
         * The conversion factor for kilometers per hour (Length.KILOMETER / Time.HOUR).
         * Represents the value of speed in kilometers per hour.
         */
        public static final double KILOMETER_PER_HOUR = Length.KILOMETER / Time.HOUR;
    }

    /**
     * Converts given value, from fromUnit to toUnit
     *
     * @param value    the value
     * @param fromUnit departure unit
     * @param toUnit   end unit
     */
    public static double convert(double value, double fromUnit, double toUnit) {
        return value * (fromUnit / toUnit);
    }

    /**
     * Converts given value, from fromUnit to the base unit
     *
     * @param value    the value
     * @param fromUnit departure unit
     */
    public static double convertFrom(double value, double fromUnit) {
        return value * fromUnit;
    }

    /**
     * Converts given value, from base unit to the fromUnit
     *
     * @param value  the value
     * @param toUnit end unit
     */
    public static double convertTo(double value, double toUnit) {
        return value * (1.00 / toUnit);
    }
}
