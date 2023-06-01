package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Represents a fixed-size window on a sequence of power samples produced by a power computer
 *
 * @author Andrei Pana 361249
 * @author David Fota 355816
 */
public final class PowerWindow {
    /**the maximum size of the window*/
    private static final int WINDOW_MAX_SIZE = 1 << 16;
    /** the size of the window*/
    private final int windowSize;

    private long position = 0;
    private long samplesDecoded = 0;
    private int[] batch1;
    private int[] batch2;
    private int index = 0;

    private final PowerComputer powerComputer;

    /**
     * the constructor of the class
     *
     * @param stream
     *      the input stream
     * @param windowSize
     *      the size of the window
     */
    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        Preconditions.checkArgument(windowSize > 0 && windowSize <= WINDOW_MAX_SIZE);
        this.windowSize = windowSize;
        batch1 = new int[WINDOW_MAX_SIZE];
        batch2 = new int[WINDOW_MAX_SIZE];
        powerComputer = new PowerComputer(stream, WINDOW_MAX_SIZE);
        samplesDecoded += powerComputer.readBatch(batch1);
    }

    /**
     * @return the window size
     */
    public int size() {
        return windowSize;
    }

    /**
     * returns the current position of the window relative to the start of the stream of power values,
     * which is initially 0 and is incremented with each call to advance
     *
     * @return the position
     */
    public long position() {
        return position;
    }

    /**
     * returns true iff the window is full, ie. that it contains as many samples as its size; this is always true,
     * except when the end of the sample stream has been reached, and the window passes it
     *
     * @return if the window is full or not
     */
    public boolean isFull() {
        return (samplesDecoded >= position + windowSize);
    }

    /**
     * returns the power sample at the given index of the window
     *
     * @param i the index
     * @return the power of the window at the given index
     * @throws IndexOutOfBoundsException if that index is not between 0 (inclusive) and window size (excluded)
     */

    public int get(int i) {
        Objects.checkIndex(i,windowSize);
        if (i + index < WINDOW_MAX_SIZE)
            return batch1[index + i];
        else return batch2[i + index - WINDOW_MAX_SIZE];
    }

    /**
     * advances the window by one sample
     *
     * @throws IOException if problem in power computer
     */

    public void advance() throws IOException {
        position++;
        index++;
        if (index + windowSize - 1 == WINDOW_MAX_SIZE) {
            samplesDecoded += powerComputer.readBatch(batch2);
        }
        if (index >= WINDOW_MAX_SIZE) {
            int[] aux = batch2;
            batch2 = batch1;
            batch1 = aux;
            index = 0;
        }
    }

    /**
     * advance the window by the given number of samples, as if the advance
     * method had been called the given number of times
     *
     * @param offset the given number of samples
     * @throws IOException if there is a problem in power computer
     * @throws IllegalArgumentException if this number is not positive or zero
     */

    public void advanceBy(int offset) throws IOException {
        Preconditions.checkArgument(offset >= 0);
        for (int i = 0; i < offset; i++) {
            advance();
        }
    }
}
