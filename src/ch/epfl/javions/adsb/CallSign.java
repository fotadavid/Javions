package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Defines the regular expression for Call Sign
 *
 * @author Andrei Pana 361249
 * @author David Fota 355816
 */

public record CallSign(String string) {

    private final static Pattern er = Pattern.compile("[A-Z0-9 ]{0,8}");

    /**
     * the compact constructor of the class
     *
     * @throws IllegalArgumentException if the string does not respect the regular expression
     */

    public CallSign {
        if (!er.matcher(string).matches()) {
            Preconditions.checkArgument(string.isEmpty());
        }
    }
}
