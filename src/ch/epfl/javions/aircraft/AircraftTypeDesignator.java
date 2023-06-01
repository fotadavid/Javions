package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Defines the regular expression for the Aircraft Type Designator
 *
 * @author Andrei Pana 361249
 * @author David Fota 355816
 */

public record AircraftTypeDesignator(String string) {

    static Pattern er = Pattern.compile("[A-Z0-9]{2,4}");

    /**
     * the compact constructor of the class
     *
     * @throws IllegalArgumentException if the string does not respect the regular expression
     */
    public AircraftTypeDesignator {
        if (!er.matcher(string).matches()) {
            Preconditions.checkArgument(string.isEmpty());
        }
    }
}
