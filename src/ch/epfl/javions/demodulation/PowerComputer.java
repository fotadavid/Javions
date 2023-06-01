package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a power computer
 *
 * @author David Fota 355816
 * @author Andrei Pana 361249
 */
public final class PowerComputer {
    private final SamplesDecoder decoder;
    private short[] signedBatch;
    private int[] current8bytes = new int[8];
    private final int batchSize;

    /**
     * The constructor of the class
     *
     * @param stream, batchSize
     *                The input stream received by the decoder
     *                Size of the batch that is to be computed
     * @throws IllegalArgumentException if the size of the batch
     *                                  is not multiple of 8 or if it is negative
     */
    public PowerComputer(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize % Byte.SIZE == 0 && batchSize > 0);
        SamplesDecoder decoder = new SamplesDecoder(stream, batchSize * 2);
        this.decoder = decoder;
        this.batchSize = batchSize;
        signedBatch = new short[batchSize * 2];
    }

    /**
     * @param batch reads from the sample decoder the number of
     *              samples needed to calculate a batch of power
     *              samples, then calculates them using the given
     *              formula and places them in the array passed as argument
     * @return the number of power samples that have been placed
     * in the table
     * @throws IOException              in case of input/output error
     * @throws IllegalArgumentException if the size of the table
     *                                  passed as argument is not equal to the size of a batch
     */
    public int readBatch(int[] batch) throws IOException {
        Preconditions.checkArgument(batch.length == batchSize);
        int count = decoder.readBatch(signedBatch);
        for (int i = 0, j = 0; i < count; i += 2, j += 2)
        {
            if( j == Long.BYTES )
                j = 0;
            current8bytes[j] = signedBatch[i];
            current8bytes[j + 1] = signedBatch[i + 1];
            batch[i / 2] = squareSumComputation(current8bytes);
        }
        return count / 2;
    }
    private int squareSumComputation(int[] current8bytes) {
        return (current8bytes[0] - current8bytes[2] + current8bytes[4] - current8bytes[6])*
                    (current8bytes[0] - current8bytes[2] + current8bytes[4] - current8bytes[6])+
                        (current8bytes[1] - current8bytes[3] + current8bytes[5] - current8bytes[7])*
                            (current8bytes[1] - current8bytes[3] + current8bytes[5] - current8bytes[7]);
    }
}
