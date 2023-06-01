package ch.epfl.javions.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

/**
 * Final class that controls the status line.
 * @author David Fota 355816
 * @author Andrei Pana 361249
 */
public final class StatusLineController {
    private final BorderPane borderPane;
    private final IntegerProperty aircraftCountProperty;
    private final LongProperty messageCountProperty;

    /**
     * Constructor of the StatusLineController
     * Creates a new BorderPane and adds the aircraftCountText and messageCountText to it
     * Binds the aircraftCountProperty and messageCountProperty to the text of the two texts
     * Sets the stylesheet of the BorderPane
     * Sets the left and right of the BorderPane
     */
    public StatusLineController(){
        borderPane = new BorderPane();
        aircraftCountProperty = new SimpleIntegerProperty();
        messageCountProperty = new SimpleLongProperty();
        borderPane.getStylesheets().add("status.css");
        borderPane.setLeft(aircraftCountText());
        borderPane.setRight(messageCountText());
    }

    /**
     * Getter for the pane of the StatusLineController
     * @return the pane of the StatusLineController
     */
    public Pane pane(){
        return borderPane;
    }

    /**
     * Getter for the aircraftCountProperty
     * @return the aircraftCountProperty
     */
    public IntegerProperty getAircraftCountProperty(){
        return aircraftCountProperty;
    }

    /**
     * Getter for the messageCountProperty
     * @return the messageCountProperty
     */
    public LongProperty getMessageCountProperty(){
        return messageCountProperty;
    }

    /**
     * Creates a new Text representing the number of visible aircraft (Aéronefs visibles in French)
     * and binds it to the aircraftCountProperty
     * @return the aircraftCountText
     */
    private Text aircraftCountText(){
        Text aircraftCountText = new Text();
        aircraftCountText.textProperty().bind(Bindings.format("Aéronefs visibles : %d", aircraftCountProperty));
        return aircraftCountText;
    }

    /**
     * Creates a new Text representing the number of messages received (Messages reçus in French)
     * and binds it to the messageCountProperty
     * @return the messageCountText
     */
    private Text messageCountText(){
        Text messageCountText = new Text();
        messageCountText.textProperty().bind(Bindings.format("Messages reçus : %d", messageCountProperty));
        return messageCountText;
    }
}
