package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Defines the regular expression for the Aircraft description
 *
 * @author Andrei Pana 361249
 * @author David Fota 355816
 */

public record AircraftDescription(String string) {

    static Pattern er = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");



    public AircraftDescription {
        if (!er.matcher(string).matches()) {
            Preconditions.checkArgument(string.isEmpty());
        }
    }
}
