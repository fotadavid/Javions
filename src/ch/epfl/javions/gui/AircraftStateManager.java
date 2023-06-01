package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static javafx.collections.FXCollections.observableSet;
import static javafx.collections.FXCollections.unmodifiableObservableSet;

/**
 * Represents a final class that manages the state of the aircraft (position, altitude, velocity, etc.)
 *
 * @author David Fota 355816
 * @author Andrei Pana 361249
 */
public final class AircraftStateManager
{
    private final static long minuteInNs = (long) 6e+10;
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> map;
    private final ObservableSet<ObservableAircraftState> states;
    private final ObservableSet<ObservableAircraftState> statesNonModifiable;
    private final AircraftDatabase database;
    private long lastProcessedTimeStamp;

    /**
     * Constructor for the AircraftStateManager
     * @param database the database
     */
    public AircraftStateManager(AircraftDatabase database){
        this.database = database;
        states = observableSet();
        statesNonModifiable = unmodifiableObservableSet(states);
        map = new HashMap<>();
    }

    /**
     * Updates the state of the aircraft with the message
     * @param message the message
     */
    public void updateWithMessage(Message message) throws IOException{
        IcaoAddress address = message.icaoAddress();
        lastProcessedTimeStamp = message.timeStampNs();
        map.putIfAbsent(address,
                new AircraftStateAccumulator<>(new ObservableAircraftState(address, database.get(address))));
        map.get(address).update(message);
        if(map.get(address).stateSetter().getPosition() != null){
            states.add(map.get(address).stateSetter());
        }
    }

    /**
     * Purges the states that are too old
     */
    public void purge(){
        states.removeIf(this::hasReceivedRecentMessage);
        map.entrySet().removeIf(entry ->
                hasReceivedRecentMessage(entry.getValue().stateSetter()));
    }
    public ObservableSet<ObservableAircraftState> states(){
        return statesNonModifiable;
    }

    private boolean hasReceivedRecentMessage(ObservableAircraftState observableAircraftState){
        return lastProcessedTimeStamp - observableAircraftState.getLastMessageTimeStampNs() > minuteInNs;
    }
}
