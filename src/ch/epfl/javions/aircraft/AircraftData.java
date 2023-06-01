package ch.epfl.javions.aircraft;

import java.util.Objects;

/**
 * Defines the regular expression for the Aircraft Data
 *
 * @author Andrei Pana 361249
 * @author David Fota 355816
 */

public record AircraftData(AircraftRegistration registration, AircraftTypeDesignator typeDesignator,
                           String model, AircraftDescription description,
                           WakeTurbulenceCategory wakeTurbulenceCategory) {
    /**
     * the compact constructor of the class
     *
     * @throws NullPointerException if one of the classes parameters is null
     */
    public AircraftData {
        Objects.requireNonNull(registration);
        Objects.requireNonNull(description);
        Objects.requireNonNull(typeDesignator);
        Objects.requireNonNull(model);
        Objects.requireNonNull(wakeTurbulenceCategory);
    }
}
