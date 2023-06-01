package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Final class that represents the controller of the table of aircraft
 * @author David Fota 355816
 * @author Andrei Pana 361249
 */
public final class AircraftTableController
{
    private static final int ICAO_ADDRESS_COLUMN_WIDTH = 60;
    private static final int CALL_SIGN_COLUMN_WIDTH = 70;
    private static final int REGISTRATION_COLUMN_WIDTH = 90;
    private static final int MODEL_COLUMN_WIDTH = 230;
    private static final int TYPE_COLUMN_WIDTH = 50;
    private static final int DESCRIPTION_COLUMN_WIDTH = 70;
    private static final int NUMERIC_COLUMN_WIDTH = 85;
    private final TableView<ObservableAircraftState> table;

    /**
     * Constructor for the table controller
     * Adds listeners to the table
     * Calls the methods to create the columns
     * @see #createAndAddTextColumns()
     * @see #createAndAddNumericColumns()
     * @param states the set of observable aircraft states
     * @param selectedState the selected state
     *                     (the one that is selected in the table)
     */
    public AircraftTableController(ObservableSet<ObservableAircraftState> states, ObjectProperty<ObservableAircraftState> selectedState)
    {
        table = new TableView<>();
        table.getStylesheets().add("/table.css");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        table.setTableMenuButtonVisible(true);

        //listeners
        selectedState.addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                table.getSelectionModel().select(newValue);
                if(oldValue == null || !oldValue.equals(newValue)) {
                    table.scrollTo(newValue);
                }
            }
        });
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                selectedState.set(newValue);
            }
        });
        states.addListener((SetChangeListener<ObservableAircraftState>)
                change -> {
                    if(change.wasAdded()) {
                        table.getItems().add(change.getElementAdded());
                        table.sort();
                    }
                    else {
                        table.getItems().removeIf(children -> children.equals(change.getElementRemoved()));
                    }
                });
        createAndAddTextColumns();
        createAndAddNumericColumns();
    }
    /**
     * Generates the text columns for the table
     * @param name the name of the column
     * @param width the width of the column
     * @param function  the function that will be applied to the cell data
     * @return  the generated column
     */
    private TableColumn<ObservableAircraftState, String> generateTextColumn(String name, int width,
                                                                           Function<ObservableAircraftState,
                                                                                   ObservableValue<String>> function)
    {
        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(name);
        column.setPrefWidth(width);
        column.setCellValueFactory(cellData -> function.apply(cellData.getValue()));
        return column;
    }

    /**
     * Generates the numeric columns for the table
     * @param name  the name of the column
     * @param function  the function that will be applied to the cell data
     * @param unit  the unit of the column
     * @param formatter the formatter that will be applied to the cell data
     * @return  the generated column
     */
    private TableColumn<ObservableAircraftState, String> generateNumericColumn(String name,
                                                                              Function<ObservableAircraftState,
                                                                                      ObservableValue<Double>> function,
                                                                               double unit,
                                                                               NumberFormat formatter)
    {
        Comparator<String> numberComparator = (o1, o2) -> {
        try {
            if( o1.isEmpty() || o2.isEmpty()) return o1.compareTo(o2);
            else {
                return Double.compare(formatter.parse(o1).doubleValue(),formatter.parse(o2).doubleValue());
            }
        } catch (ParseException e) {throw new Error(e);}
    };
        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(name);
        column.getStyleClass().add("numeric");
        column.setPrefWidth(NUMERIC_COLUMN_WIDTH);
        column.setCellValueFactory(cellData -> function.apply(cellData.getValue()).
                map(k -> Double.isNaN(k) ? "" : formatter.format(Units.convertTo(k, unit))));
        column.setComparator(numberComparator);
        return column;
    }

    /**
     * Creates and adds the text columns to the table
     * @see #generateTextColumn(String, int, Function)
     * @see #table
     */
    private void createAndAddTextColumns(){
        TableColumn<ObservableAircraftState, String> addressColumn = generateTextColumn("OACI",
                ICAO_ADDRESS_COLUMN_WIDTH, cellData -> new ReadOnlyObjectWrapper<>(cellData.getAddress())
                        .map(IcaoAddress::string));
        TableColumn<ObservableAircraftState, String> callSignColumn = generateTextColumn("Indicatif",
                CALL_SIGN_COLUMN_WIDTH, cellData -> cellData.callSignProperty().map(CallSign::string));
        TableColumn<ObservableAircraftState, String> registrationColumn = generateTextColumn("Immatriculation",
                REGISTRATION_COLUMN_WIDTH, cellData -> new ReadOnlyObjectWrapper<>(cellData.getData())
                        .map(AircraftData::registration).map(AircraftRegistration::string));
        TableColumn<ObservableAircraftState, String> modelColumn = generateTextColumn("Modèle",
                MODEL_COLUMN_WIDTH, cellData -> new ReadOnlyObjectWrapper<>(cellData.getData())
                        .map(AircraftData::model));
        TableColumn<ObservableAircraftState, String> typeColumn = generateTextColumn("Type", TYPE_COLUMN_WIDTH,
                cellData -> new ReadOnlyObjectWrapper<>(cellData.getData()).map(AircraftData::typeDesignator)
                        .map(AircraftTypeDesignator::string));
        TableColumn<ObservableAircraftState, String> descriptionColumn = generateTextColumn("Description",
                DESCRIPTION_COLUMN_WIDTH, cellData -> new ReadOnlyObjectWrapper<>(cellData.getData())
                        .map(AircraftData::description).map(AircraftDescription::string));
        table.getColumns().addAll(addressColumn, callSignColumn, registrationColumn, modelColumn, typeColumn,
                descriptionColumn);
    }

    /**
     * Creates and adds the numeric columns to the table
     * @see #generateNumericColumn(String, Function, double, NumberFormat)
     * @see #table
     */
    private void createAndAddNumericColumns(){
        NumberFormat formatterLongitudeLatitude = NumberFormat.getInstance();
        formatterLongitudeLatitude.setMinimumFractionDigits(4);
        formatterLongitudeLatitude.setMaximumFractionDigits(4);
        NumberFormat formatterAltitudeAndVelocity = NumberFormat.getInstance();
        formatterAltitudeAndVelocity.setMaximumFractionDigits(0);
        TableColumn<ObservableAircraftState, String> longitudeColumn = generateNumericColumn("Longitude (°)",
                cellData -> cellData.positionProperty().map(GeoPos::longitude), Units.Angle.DEGREE,
                formatterLongitudeLatitude);
        TableColumn<ObservableAircraftState, String> latitudeColumn = generateNumericColumn("Latitude (°)",
                cellData -> cellData.positionProperty().map(GeoPos::latitude), Units.Angle.DEGREE,
                formatterLongitudeLatitude);
        TableColumn<ObservableAircraftState, String> altitudeColumn = generateNumericColumn("Altitude (m)",
                cellData -> cellData.altitudeProperty().map(Number::doubleValue), Units.Length.METER,
                formatterAltitudeAndVelocity);
        TableColumn<ObservableAircraftState, String> velocityColumn = generateNumericColumn("Vitesse (km/h)",
                cellData -> cellData.velocityProperty().map(Number::doubleValue),
                Units.Speed.KILOMETER_PER_HOUR, formatterAltitudeAndVelocity);
        table.getColumns().addAll(longitudeColumn, latitudeColumn, altitudeColumn, velocityColumn);
    }

    /**
     * Getter for the pane of the table
     * @return the pane of the table
     */
    public TableView<ObservableAircraftState> pane()
    {
        return table;
    }

    /**
     * Sets the action to do when a row is double-clicked
     * @param consumer the action to do
     */
    public void setOnDoubleClick(Consumer<ObservableAircraftState> consumer)
    {
        table.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY){
                ObservableAircraftState state = table.getSelectionModel().getSelectedItem();
                if(state != null){
                    consumer.accept(state);
                }
            }
        });
    }
}
