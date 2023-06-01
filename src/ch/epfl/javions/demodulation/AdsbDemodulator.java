package ch.epfl.javions.demodulation;

import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Represents an ADSB Demodulator
 *
 * @author Andrei Pana 361249
 * @author David Fota 355816
 */
public final class AdsbDemodulator {
    PowerWindow window;
    private int sumPCurrent, sumPPrevious, sumPAfter, sumV;
    byte[] byteArray = new byte[RawMessage.LENGTH];
    private final int NANO_PER_POS = 100;
    private final int WINDOW_SIZE = 1200;
    private final int PREAMBLE_SIZE = 80;
    private final int DOWNLINK_CORRECT_VALUE = 17;
    private final int TOTAL_BYTES = 112;
    private final int[] messageValleyIndexes = {5, 15, 20, 25, 30, 40};
    private final int[] messageAfterIndexes = {1, 11, 36, 46};
    private final int[] messageCurrentIndexes = {0, 10, 35, 45};

    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        window = new PowerWindow(samplesStream, WINDOW_SIZE);
        sumPCurrent = computeSum(messageCurrentIndexes);
        sumPPrevious = 0;
    }

    /**
     * Calculates and demodulates the next message that is found
     *
     * @return message, which represents the calculated Raw Message
     */
    public RawMessage nextMessage() throws IOException {

        while (window.isFull()) {
            sumPAfter = computeSum(messageAfterIndexes);
            if (isEligibleForSumValleyCalculation()) {
                sumV = computeSum(messageValleyIndexes);
                if (isValid()) {
                    Arrays.fill(byteArray, (byte) 0);
                    for( int i = 0; i < TOTAL_BYTES; i += Long.BYTES)
                    {
                        fillByteArray(i);
                    }
                    var message = RawMessage.of(window.position() * NANO_PER_POS, byteArray);
                    if (message != null && message.downLinkFormat() == DOWNLINK_CORRECT_VALUE) {
                        window.advanceBy(WINDOW_SIZE - 1);
                        sumPPrevious = computeSum(messageCurrentIndexes);
                        sumPCurrent = computeSum(messageAfterIndexes);
                        window.advance();
                        return message;
                    }
                }
            }
            sumPPrevious = sumPCurrent;
            sumPCurrent = sumPAfter;
            window.advance();
        }
        return null;
    }
    private int computeSum( int[] indexes ){
        int s = 0;
        for( int i : indexes)
            s += window.get(i);
        return s;
    }
    private void fillByteArray(int i)
    {
        for( int j = 0; j < Long.BYTES; j++ )
        {
            byteArray[i / Long.BYTES] |= (testBitValue(i + j) << (7 - j));
        }
    }
    private byte testBitValue(int index) {
        if (window.get(PREAMBLE_SIZE + 10 * index) >= window.get((PREAMBLE_SIZE + 5) + 10 * index))
            return 1;
        else
            return 0;
    }
    private boolean isEligibleForSumValleyCalculation(){
        return sumPCurrent > sumPPrevious && sumPCurrent > sumPAfter;
    }
    private boolean isValid(){
        return sumPCurrent >= 2 * sumV;
    }
}