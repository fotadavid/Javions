package ch.epfl.javions;

import java.util.Objects;

/**
 * contains methods to extract a subset of the 64 bits
 * from a value of type long
 *
 * @author Andrei Pana 361249
 * @author David Fota 355816
 */

public final class Bits {
    private Bits() {}

    /**
     * extracts from the 64-bit vector "value" the range of "size" bits
     * which it interprets as an unsigned value
     *
     * @param value the value
     * @param start start index
     * @param size  size of the vector
     * @return the wanted vector
     * @throws IllegalArgumentException  if the size is not
     *                                   strictly greater than 0 and strictly less than 32
     * @throws IndexOutOfBoundsException if the range described
     *                                   by start and size is not entirely
     *                                   between 0 (inclusive) and 64 (exclusive)
     */
    public static int extractUInt(long value, int start, int size) {
        Preconditions.checkArgument(size > 0 && size < Integer.SIZE);
        Objects.checkFromIndexSize(start, size, Long.SIZE);
        return (int) (((1 << size) - 1) & (value >>> start));
    }

    /**
     * @param index the index
     * @param value the given value
     * @throws IndexOutOfBoundsException if the index is
     *                                   not between 0 (inclusive) and 64 (exclusive).
     * @return true iff the given index value bit is 1
     */
    public static boolean testBit(long value, int index) {
        Objects.checkIndex(index, Long.SIZE);
        return (value & (1L << index)) != 0;
    }
}
