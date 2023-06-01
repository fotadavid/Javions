package ch.epfl.javions.aircraft;

/**
 * Represents the wake turbulence category of an aircraft
 *
 * @author Andrei Pana 361249
 * @author David Fota 355816
 */

public enum WakeTurbulenceCategory {
    LIGHT,
    MEDIUM,
    HEAVY,
    UNKNOWN;

    /**
     * @param s the string
     * @return the wake turbulence category corresponding to the given string
     */
    public static WakeTurbulenceCategory of(String s) {
        return switch (s) {
            case "L" -> LIGHT;
            case "M" -> MEDIUM;
            case "H" -> HEAVY;
            case default -> UNKNOWN;
        };
    }
}
