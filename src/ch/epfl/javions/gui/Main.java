package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

import static java.lang.Thread.sleep;

/**
 * Main class of the application.
 * @author David Fota 355816
 * @author Andrei Pana 361249
 */
public class Main extends Application
{
    private static final long PURGE_INTERVAL = 1_000_000_000L;
    private static final int ZOOM_LEVEL = 8;
    private static final int MIN_X_VALUE = 33530;
    private static final int MIN_Y_VALUE = 23070;
    private static final long NANO_TO_MILI = 1_000_000L;
    private static final int MIN_HEIGHT = 600;
    private static final int MIN_WIDTH = 800;
    private static final String SERVER_URL = "tile.openstreetmap.org";
    private static final String TILE_CACHE = "tile-cache";
    private static final String AIRCRAFT_FOLDER_ZIPPED = "/aircraft.zip";
    private static final String TITLE = "Javions";

    /**
     * Main method of the application.
     * @param args the command line arguments
     */
    public static void main(String[] args) { launch(args); }

    /**
     * Starts the application.
     * @param primaryStage the primary stage for this application, onto which
     *        the application scene can be set.
     * @throws Exception if something goes wrong during the start.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        ConcurrentLinkedQueue<Message> messageQueue = new ConcurrentLinkedQueue<>();
        AircraftStateManager asm = new AircraftStateManager(database());
        StatusLineController slc = new StatusLineController();

        Scene scene = createScene(asm, slc);
        configurePrimaryStage(primaryStage, scene);

        Thread thread;
        if(getParameters().getRaw().isEmpty()) {
            Supplier<Message> supplier = stantardInputSupplier();
            thread = new Thread(() -> getFromSupplier(supplier, messageQueue));
        } else {
            Supplier<Message> supplier = fromFileInputSupplier(getParameters().getRaw().get(0));
            thread = new Thread(() -> getFromSupplier(supplier, messageQueue));
        }
        thread.setDaemon(true);
        thread.start();
        new AnimationTimer() {
            private long lastPurge = 0L;
            @Override
            public void handle(long now) {
                try{
                    while(!messageQueue.isEmpty()){
                        Message msg = messageQueue.remove();
                        asm.updateWithMessage(msg);
                        slc.getMessageCountProperty().set(slc.getMessageCountProperty().get() + 1);
                    }
                    if(now - lastPurge >= PURGE_INTERVAL){
                        lastPurge = now;
                        asm.purge();
                    }
            } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();

    }
    /**
     * Reads the messages from the file.
     * @param name the name of the file
     * @return the list of messages
     * @throws IOException if an I/O error occurs
     */
    private static List<Message> readMessages(String name) throws IOException {
        List<Message> messages = new ArrayList<>();
        try(DataInputStream dis = new DataInputStream(
                new BufferedInputStream(
                    new FileInputStream(name)))) {
            byte[] buffer = new byte[RawMessage.LENGTH];
            //noinspection InfiniteLoopStatement
            while(true){
                long tstp = dis.readLong();
                int byteCount = dis.readNBytes(buffer, 0, buffer.length);
                assert byteCount == RawMessage.LENGTH;
                ByteString bs = new ByteString(buffer);
                RawMessage rmsg = new RawMessage(tstp, bs);
                messages.add(MessageParser.parse(rmsg));
            }
        }catch(EOFException e) {
            return messages;
        }
    }

    /**
     * Creates supplier for the standard input.
     * @return the supplier
     * @throws IOException if an I/O error occurs
     */
    private static Supplier<Message> stantardInputSupplier() throws IOException {
        AdsbDemodulator adsb = new AdsbDemodulator(System.in);
        return () -> {
            try {
                while(true) {
                    RawMessage rmsg = adsb.nextMessage();
                    if (rmsg == null)
                        return null;
                    Message msg = MessageParser.parse(rmsg);
                    if(msg != null)
                        return msg;
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    /**
     * Creates supplier for the file input.
     * @param name the name of the file
     * @return the supplier
     * @throws IOException if an I/O error occurs
     */
    private static Supplier<Message> fromFileInputSupplier(String name) throws IOException {
        long start_time = System.nanoTime();
        List<Message> messages = readMessages(name);
        Iterator<Message> iterator = messages.iterator();
        return () -> {
            try {
                Message msg;
                if(iterator.hasNext())
                    msg = iterator.next();
                else
                    return null;
                long messsageTimeStamp = msg.timeStampNs();
                long currentTime = System.nanoTime() - start_time;
                if(currentTime < messsageTimeStamp)
                    sleep((messsageTimeStamp - currentTime) / NANO_TO_MILI);
                return msg;
            } catch(InterruptedException ignored) {
                throw new Error();
            }
        };
    }

    /**
     * Gets the message from the supplier and adds it to the queue.
     * @param supplier the supplier
     * @param messageQueue the queue
     */
    private void getFromSupplier(Supplier<Message> supplier, ConcurrentLinkedQueue<Message> messageQueue){
        while(true){
            Message msg = supplier.get();
            if(msg != null)
                messageQueue.add(msg);
        }
    }

    /**
     * Configures the primary stage.
     * @param primaryStage the primary stage
     * @param scene the scene
     */
    private void configurePrimaryStage(Stage primaryStage, Scene scene) {
        primaryStage.setTitle(TITLE);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Creates the scene.
     * @param asm the aircraft state manager.
     * @param slc the status line controller.
     * @return the scene
     */
    private Scene createScene(AircraftStateManager asm, StatusLineController slc){
        MapParameters mp = new MapParameters(ZOOM_LEVEL, MIN_X_VALUE, MIN_Y_VALUE);
        ObjectProperty<ObservableAircraftState> sap = new SimpleObjectProperty<>();
        AircraftController ac = new AircraftController(mp, asm.states(), sap);
        Path tileCache = Path.of(TILE_CACHE);
        TileManager tm = new TileManager(tileCache, SERVER_URL);
        BaseMapController bmc = new BaseMapController(tm, mp);
        AircraftTableController atc = new AircraftTableController(asm.states(), sap);
        atc.setOnDoubleClick(s -> bmc.centerOn(s.getPosition()));
        slc.getAircraftCountProperty().bind(Bindings.size(asm.states()));
        StackPane stp = new StackPane(bmc.pane(), ac.pane());
        BorderPane bp = new BorderPane(atc.pane());
        bp.setTop(slc.pane());
        SplitPane sp = new SplitPane(stp, bp);
        sp.setOrientation(javafx.geometry.Orientation.VERTICAL);
        return new Scene(sp);
    }

    /**
     * Creates the database from the zip in "/aircraft.zip".
     * @return  the database
     * @throws URISyntaxException if the URI is invalid
     */
    private AircraftDatabase database() throws URISyntaxException{
        URL u = getClass().getResource(AIRCRAFT_FOLDER_ZIPPED);
        assert u != null;
        Path p = Path.of(u.toURI());
        return new AircraftDatabase(p.toString());
    }
}
