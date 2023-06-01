package ch.epfl.javions;

/**
 * Represents the preconditions
 *
 * @author Andrei Pana 361249
 * @author David Fota 355816
 */
public final class Preconditions {
    private Preconditions() {
    }

    /**
     * Checks the given argument
     *
     * @throws IllegalArgumentException if the boolean expression given is false
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}
