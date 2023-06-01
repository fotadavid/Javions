package ch.epfl.javions.gui;

import javafx.scene.image.Image;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;

/**
 * Represents a final class that manages the tiles of the map.
 * @author David Fota 355816
 * @author Andrei Pana 361249
 */
public final class TileManager {
    /**
     * Inner record representing a tileId with a zoom, x and y.
     * @param zoom the zoom level
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public record TileId(int zoom, int x, int y){
        public boolean isValid(){
            return (x >= 0) && (x < (1 << zoom)) && (y >= 0) && (y < (1 << zoom));
        }
    }
    private final Path localCache;
    private final String serverUrl;
    private final static int MAX_CACHE_CAPACITY = 100;
    private final static float LOAD_FACTOR = 0.75f;
    private final LinkedHashMap<TileId, Image> cache;

    /**
     * Constructs a TileManager with the given localCache and serverUrl.
     * @param localCache the local cache
     * @param serverUrl the server url
     */
    public TileManager(Path localCache, String serverUrl) {
        this.localCache = localCache;
        this.serverUrl = serverUrl;
        cache = new LinkedHashMap<>(MAX_CACHE_CAPACITY, LOAD_FACTOR, true);
    }

    /**
     * Returns the image for the tile at the given tileId.
     * If the image is not in the cache, it is downloaded from the server and stored in the cache.
     * If the cache is full, the least recently used image is removed from the cache.
     * @param tileId the ID of the tile
     * @return the image for the tile at the given tileId
     * @throws IOException if an I/O error occurs
     */
    public Image imageForTileAt(TileId tileId) throws IOException {
        if(cache.containsKey(tileId)){
            return cache.get(tileId);
        }
        Path path = Path.of(localCache.toString(),tileRelatedString(tileId));
        if(cache.size() == MAX_CACHE_CAPACITY){
            cache.remove(cache.keySet().iterator().next());
        }
        if(Files.exists(path)){
            try(FileInputStream is = new FileInputStream(path.toFile())){
                Image image = new Image(is);
                cache.put(tileId,image);
                return image;
            }
        }else{
            return getImageFromWebsite(path, tileId);
        }
    }

    /**
     * Retrieves an image from a website given the URL constructed from the server URL and tile ID.
     * The image is downloaded from the website and stored locally at the specified path.
     *
     * @param path    The path where the image will be stored.
     * @param tileId  The tile ID used to construct the URL for retrieving the image.
     * @return The downloaded image as an Image object.
     * @throws IOException if an I/O error occurs while downloading or storing the image.
     */

    private Image getImageFromWebsite(Path path, TileId tileId) throws IOException{
        URL u = new URL("https://" + serverUrl + "/" + tileRelatedString(tileId));
        URLConnection c = u.openConnection();
        Files.createDirectories(path.getParent());
        c.setRequestProperty("User-Agent", "Javions");
        try(InputStream is = c.getInputStream() ; FileOutputStream o  = new FileOutputStream(path.toFile())){
            byte[] buffer = is.readAllBytes();
            o.write(buffer);
            Image image = new Image(new ByteArrayInputStream(buffer));
            cache.put(tileId , image);
            return image;
        }
    }
    /**
     * Constructs a string representation of the tile ID in the format "zoom/x/y.png".
     *
     * @param tileId The tile ID used to construct the string.
     * @return The string representation of the tile ID.
     */
    private String tileRelatedString(TileId tileId){
        return tileId.zoom() + "/" + tileId.x() + "/" + tileId.y() + ".png";
    }
}
