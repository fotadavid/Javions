package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.WakeTurbulenceCategory;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

import static javafx.scene.paint.CycleMethod.NO_CYCLE;


public final class AircraftController {
    /**
     * Offset between  text an label border
     */
    private final static int LABEL_BORDER_OFFSET = 4;

    /**
     * Minimum zoom for which tags are visible
     */
    private final static int MIN_ZOOM_FOR_VISIBLE_TAGS = 11;


    private final Pane pane;
    private final MapParameters mapParameters;
    private final ObjectProperty<ObservableAircraftState> aircraftStateObjectProperty;

    /**
     * Constructs an AircraftController with the given parameters.
     *
     * @param mapParameters             the map parameters used for positioning aircraft on the map
     * @param aircraftStates            the set of observable aircraft states
     * @param aircraftStateObjectProperty the object property representing the current aircraft state
     */

    public AircraftController(MapParameters mapParameters,
                              ObservableSet<ObservableAircraftState> aircraftStates,
                              ObjectProperty<ObservableAircraftState> aircraftStateObjectProperty) {

        this.mapParameters = mapParameters;
        this.aircraftStateObjectProperty = aircraftStateObjectProperty;

        pane = new Pane();
        pane.getStylesheets().add("/aircraft.css");
        pane.setPickOnBounds(false);

        aircraftStates.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if (change.wasAdded()) constructAircraftGroup(change.getElementAdded());

            else pane.getChildren().removeIf(children ->
                        children.getId().equals(change.getElementRemoved().getAddress().string()));
        });

    }

    /**
     * Returns the Pane associated with the AircraftController.
     *
     * @return the Pane object used for displaying the aircraft
     */

    public Pane pane() {
        return pane;
    }


    /**
     * Returns the color of the aircraft based on its altitude.
     *
     * @param altitude the altitude of the aircraft
     * @return the color representing the aircraft's altitude
     */
    private static Color getPlaneColor(double altitude) {
        return ColorRamp.PLASMA.at(Math.pow(altitude / 12000, 1d / 3d));
    }

    /**
     * Constructs the group for an aircraft and adds it to the pane.
     *
     * @param state the observable aircraft state for which to construct the group
     */

    private void constructAircraftGroup(ObservableAircraftState state) {
        Group aircraftGroup = new Group();
        aircraftGroup.setId(state.getAddress().string());
        aircraftGroup.viewOrderProperty().bind(state.altitudeProperty().negate());
        aircraftGroup.getChildren().addAll(trajectoryGroup(state), iconAndTagGroup(state));
        pane.getChildren().add(aircraftGroup);
    }

    /**
     * Creates a group containing the SVG icon and label for an aircraft.
     * The position of this group is bound to the zoom level and the position of the aircraft.
     *
     * @param state the observable aircraft state for which to create the icon and label group
     * @return the group containing the SVG icon and label for the aircraft
     */
    private Group iconAndTagGroup(ObservableAircraftState state) {
        Group iconAndTag = new Group(getSVG(state), labelGroup(state));


        iconAndTag.layoutXProperty().bind(Bindings.createDoubleBinding(() ->
                WebMercator.x(mapParameters.getZoomValue(),
                        state.getPosition().longitude()) - mapParameters.getMinXValue(),
                mapParameters.zoomProperty(),
                state.positionProperty(),
                mapParameters.minXProperty()));

        iconAndTag.layoutYProperty().bind(Bindings.createDoubleBinding(() ->
                WebMercator.y(mapParameters.getZoomValue(),
                        state.getPosition().latitude()) - mapParameters.getMinYValue(),
                mapParameters.zoomProperty(),
                state.positionProperty(),
                mapParameters.minYProperty()));


        iconAndTag.setOnMousePressed(e -> aircraftStateObjectProperty.set(state));

        return iconAndTag;
    }


    /**
     * Returns the SVGPath object representing the aircraft icon based on the aircraft data and properties.
     *
     * @param state the observable aircraft state for which to get the SVGPath
     * @return the SVGPath object representing the aircraft icon
     */
    private SVGPath getSVG(ObservableAircraftState state) {
        AircraftData aircraftData = state.getData();

        AircraftTypeDesignator typeDesignator = (aircraftData == null) ?
                new AircraftTypeDesignator("") : aircraftData.typeDesignator();

        AircraftDescription aircraftDescription = (aircraftData == null)
                ? new AircraftDescription("") : aircraftData.description();

        WakeTurbulenceCategory wakeTurbulenceCategory = (aircraftData == null)
                ? WakeTurbulenceCategory.UNKNOWN : aircraftData.wakeTurbulenceCategory();

        SVGPath iconPath = new SVGPath();

        ObservableValue<AircraftIcon> icon = state.categoryProperty().map(c ->
                AircraftIcon.iconFor(typeDesignator, aircraftDescription, c.intValue(), wakeTurbulenceCategory));

        // Icon related Bindings

        iconPath.getStyleClass().add("aircraft");
        iconPath.contentProperty().bind(icon.map(AircraftIcon::svgPath));
        iconPath.fillProperty().bind(Bindings.createObjectBinding(() ->
                getPlaneColor(state.altitudeProperty().get()),
                state.altitudeProperty()));
        iconPath.rotateProperty().bind(Bindings.createDoubleBinding(() ->
                (icon.getValue().canRotate()) ? Units.convertTo(state.getTrackOrHeading(), Units.Angle.DEGREE) : 0,
                state.trackOrHeadingProperty()));
        return iconPath;
    }

    /**
     * Returns the group containing the label for an aircraft.
     *
     * @param state the observable aircraft state for which to create the label group
     * @return the group containing the label for the aircraft
     */

    private Group labelGroup(ObservableAircraftState state) {
        Text text = new Text();
        Rectangle rectangle = new Rectangle();

        //rectangle related bindings
        rectangle.widthProperty().bind(text.layoutBoundsProperty().map(b -> b.getWidth() + LABEL_BORDER_OFFSET));
        rectangle.heightProperty().bind(text.layoutBoundsProperty().map(b -> b.getHeight() + LABEL_BORDER_OFFSET));

        //text related bindings
        text.textProperty().bind(Bindings.format("%s \n%s km/h\u2002%s m",
                aircraftIdentification(state),
                velocity(state),
                altitude(state)));


        Group labelGroup = new Group(rectangle, text);
        labelGroup.getStyleClass().add("label");
        labelGroup.visibleProperty().bind(aircraftStateObjectProperty.isEqualTo(state)
                .or(mapParameters.zoomProperty().greaterThanOrEqualTo(MIN_ZOOM_FOR_VISIBLE_TAGS))
        );

        return labelGroup;
    }


    /**
     * Returns the group containing the trajectory lines for an aircraft.
     *
     * @param state the observable aircraft state for which to create the trajectory group
     * @return the group containing the trajectory lines for the aircraft
     */

    private Group trajectoryGroup(ObservableAircraftState state) {
        Group trajectoryGroup = new Group();
        trajectoryGroup.getStyleClass().add("trajectory");
        trajectoryGroup.layoutXProperty().bind(mapParameters.minXProperty().negate());
        trajectoryGroup.layoutYProperty().bind(mapParameters.minYProperty().negate());
        trajectoryGroup.visibleProperty().bind(Bindings.createBooleanBinding(() ->
                state == aircraftStateObjectProperty.get(), aircraftStateObjectProperty));


        //trajectory listener
        InvalidationListener trajectoryChangeListener = z -> redrawTrajectory(trajectoryGroup, state);


        mapParameters.zoomProperty().addListener(trajectoryChangeListener);

        trajectoryGroup.visibleProperty().addListener((o, oV, nV) -> {
            if (nV) {
                redrawTrajectory(trajectoryGroup, state);
                state.trajectoryProperty().addListener(trajectoryChangeListener);
            } else {
                state.trajectoryProperty().removeListener(trajectoryChangeListener);
            }
        });
        return trajectoryGroup;
    }

    /**
     * Redraws the trajectory lines for an aircraft.
     *
     * @param trajectoryGroup the group containing the trajectory lines
     * @param state the observable aircraft state for which to redraw the trajectory
     */

    private void redrawTrajectory(Group trajectoryGroup, ObservableAircraftState state) {
        trajectoryGroup.getChildren().clear();
        trajectoryGroup.getChildren().addAll(getAllTrajectoryLines(state.getTrajectory()));
    }

    /**
     * Computes all the trajectory lines from a list of given positions
     * @param airbornePositions the list of positions
     * @return the list of lines from which the trajectory is made
     */
    private List<Line> getAllTrajectoryLines(List<ObservableAircraftState.AirbornePos> airbornePositions) {
        ArrayList<Line> lines = new ArrayList<>();
        for (int i = 1; i < airbornePositions.size(); i++) {
            lines.add(createLine(airbornePositions.get(i - 1), airbornePositions.get(i)));
        }
        return lines;
    }

    /**
     * Creates a line between two positions on the map.
     *
     * @param pos1 the first position
     * @param pos2 the second position
     * @return the line connecting the two positions
     */

    private Line createLine(ObservableAircraftState.AirbornePos pos1, ObservableAircraftState.AirbornePos pos2) {
        GeoPos firstPoint = pos1.position();
        GeoPos secondPoint = pos2.position();
        Line line = new Line(WebMercator.x(mapParameters.getZoomValue(), firstPoint.longitude()),
                WebMercator.y(mapParameters.getZoomValue(), firstPoint.latitude()),
                WebMercator.x(mapParameters.getZoomValue(), secondPoint.longitude()),
                WebMercator.y(mapParameters.getZoomValue(), secondPoint.latitude()));

        if (pos1.altitude() == pos2.altitude()) {
            line.setStroke(getPlaneColor(pos1.altitude()));
        } else {
            Stop s1 = new Stop(0, getPlaneColor(pos1.altitude()));
            Stop s2 = new Stop(1, getPlaneColor(pos2.altitude()));
            line.setStroke(
                    new LinearGradient(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY(),
                            true, NO_CYCLE, s1, s2));
        }
        return line;
    }

    /**
     * Returns the identification string for an aircraft.
     *
     * @param state the observable aircraft state for which to get the identification
     * @return the identification string for the aircraft
     */

    private ObservableValue<String> aircraftIdentification(ObservableAircraftState state) {
        return (state.getData() != null) ? new SimpleStringProperty(state.getData().registration().string()) :
                Bindings.when(state.callSignProperty().isNotNull())
                        .then(Bindings.convert(state.callSignProperty().map(CallSign::string)))
                        .otherwise(state.getAddress().string());
    }

    /**
     * Returns the velocity string for an aircraft.
     *
     * @param state the observable aircraft state for which to get the velocity
     * @return the velocity string for the aircraft
     */

    private ObservableValue<String> velocity(ObservableAircraftState state) {
        return state.velocityProperty().map(v -> (v.doubleValue() != 0 || !Double.isNaN(v.doubleValue())) ?
                String.format("%.0f", Units.convertTo(v.doubleValue(), Units.Speed.KILOMETER_PER_HOUR)) : "?");
    }

    /**
     * Returns the altitude string for an aircraft.
     *
     * @param state the observable aircraft state for which to get the altitude
     * @return the altitude string for the aircraft
     */

    private ObservableValue<String> altitude(ObservableAircraftState state) {
        return state.altitudeProperty().map(v ->
                (v.doubleValue() != 0 || !Double.isNaN(v.doubleValue())) ?
                        String.format("%.0f", v.doubleValue()) : "?");
    }
}
