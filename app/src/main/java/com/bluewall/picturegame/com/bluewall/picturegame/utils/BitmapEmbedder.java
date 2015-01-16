package com.bluewall.picturegame.com.bluewall.picturegame.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;

public class BitmapEmbedder {

    //Number of times the redundancy level is repeated in the header
    private static final int BASE = 5;

    /**
     * Embeds A string inside a bitmap while minimizing the visible change to the image
     *
     * @param bitmap  The bitmap in which the message shall be encoded
     * @param message The message to be encoded
     * @return A new bitmap with the message encoded
     * @throws EncodeFailureException Message could not be encoded into bitmap, often due to a large message or a small bitmap
     */
    public static Bitmap embed(Bitmap bitmap, String message) throws EncodeFailureException {

        //Method only allocates 2 bytes for message length in header
        if (message.length() > Short.MAX_VALUE)
            throw new EncodeFailureException();

        //Partitions message length into 2 bytes
        byte lower = (byte) (0xff & message.length());
        byte upper = (byte) (0xff & (message.length() >> 8));

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] buffer = new int[w * h];
        bitmap.getPixels(buffer, 0, w, 0, 0, w, h);

        Bitmap ret;
        byte redundancyLevel = 0;
        boolean success;
        do {
            redundancyLevel++;

            //Bitmap does not have enough pixels at current redundancy level to encode message
            if (redundancyLevel * (message.length() + 2) + BASE >= w * h) {
                throw new EncodeFailureException();
            }

            //Encodes redundancy level into first 4*BASE bytes of bitmap
            for (int i = 0; i < BASE; ++i) {
                buffer[i] = writeDepthTwo(buffer[i], redundancyLevel);
            }

            //Encodes message length into following 2*redundancyLevel of bitmap
            for (int r = 0; r < redundancyLevel; ++r) {
                buffer[BASE + r] = writeDepthTwo(buffer[BASE + r], upper);
                buffer[BASE + redundancyLevel + r] = writeDepthTwo(buffer[BASE + redundancyLevel + r], lower);
            }
            //Encodes rest of message
            for (int i = 0; i < message.length(); ++i) {
                for (int r = 0; r < redundancyLevel; ++r) {
                    buffer[r + BASE + (i + 2) * redundancyLevel] = writeDepthTwo(buffer[r + BASE + (i + 2) * redundancyLevel], message.charAt(i));// + new Random().nextInt(5000000);
                }
            }

            ret = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            ret.setPixels(buffer, 0, w, 0, 0, w, h);

            try {
                success = message.equals(decode(ret));
            } catch (DecodeFailureException e) {
                success = false;
            }

        } while (!success);

        return ret;
    }

    /**
     * Decodes a message from a bitmap, if no message was encoded, method may either throw an exception or output gibberish (false positive)
     *
     * @param bitmap Bitmap to decode
     * @return Decoded message
     * @throws DecodeFailureException
     */
    public static String decode(Bitmap bitmap) throws DecodeFailureException {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] buffer = new int[w * h];
        bitmap.getPixels(buffer, 0, w, 0, 0, w, h);

        //Extracting the redundancy level from the first 4*BASE bytes of the the of the image
        List<Byte> li = new ArrayList<Byte>();
        for (int i = 0; i < BASE; ++i) {
            li.add(decodeAsByte(buffer[i]));
        }
        byte redundancyLevel = mode(li).get(0);

        //Extracting the upper 8 bits of the short integer representing integer length
        li.clear();
        for (int r = 0; r < redundancyLevel; ++r) {
            li.add(decodeAsByte(buffer[BASE + r]));
        }
        int upper = mode(li).get(0);

        //Extracting the lower 8 bits of the short integer representing integer length
        li.clear();
        for (int r = 0; r < redundancyLevel; ++r) {
            li.add(decodeAsByte(buffer[BASE + redundancyLevel + r]));
        }
        int lower = mode(li).get(0);

        //Merge the upper and lower 8 bytes, uses int in place of short to remedy lack of unsigned short
        int len = (upper << 8) + lower;

        if (BASE + (len + 2) * redundancyLevel >= buffer.length) {
            throw new DecodeFailureException();
        }

        List<Character> clist = new ArrayList<Character>();
        StringBuilder ret = new StringBuilder("");
        for (int i = 0; i < len; ++i) {
            clist.clear();
            for (int r = 0; r < redundancyLevel; ++r) {
                clist.add(decodeAsChar(buffer[r + BASE + (i + 2) * redundancyLevel]));
            }
            ret.append(mode(clist).get(0));
        }
        return ret.toString();
    }

    /*
     * Decodes single character from pixel
     */
    private static char decodeAsChar(int pixel) {
        int ret = 0;
        for (int i = 0; i < 4; ++i) {
            int bits = ((pixel >> i * 8) & 0b11);
            ret |= (bits << 2 * (i));
        }
        return (char) ret;
    }

    /*
     * Decodes single byte from pixel
     */
    private static byte decodeAsByte(int pixel) {
        int ret = 0;
        for (int i = 0; i < 4; ++i) {
            int bits = ((pixel >> i * 8) & 0b11);
            ret |= (bits << 2 * (i));
        }
        return (byte) ret;
    }

    /*
     * Writes single character to pixel
     */
    private static int writeDepthTwo(int pixel, char value) {
        int ret = pixel;
        for (int i = 0; i < 4; ++i) {
            int partial = (((int) value >> i * 2) & 0b11);
            ret &= ~(0b11 << i * 8);
            ret |= (partial << i * 8);
        }
        return ret;
    }

    /*
     * Writes single byte to pixel
     */
    private static int writeDepthTwo(int pixel, byte value) {
        int ret = pixel;
        for (int i = 0; i < 4; ++i) {
            int partial = (((int) value >> i * 2) & 0b11);
            ret &= ~(0b11 << i * 8);
            ret |= (partial << i * 8);
        }
        return ret;
    }

    /*
     * Calculates the mode value in a List
     * Code taken from http://rosettacode.org/wiki/Averages/Mode#Java
     */
    private static <T> List<T> mode(List<? extends T> coll) {
        Map<T, Integer> seen = new HashMap<T, Integer>();
        int max = 0;
        List<T> maxElems = new ArrayList<T>();
        for (T value : coll) {
            if (seen.containsKey(value))
                seen.put(value, seen.get(value) + 1);
            else
                seen.put(value, 1);
            if (seen.get(value) > max) {
                max = seen.get(value);
                maxElems.clear();
                maxElems.add(value);
            } else if (seen.get(value) == max) {
                maxElems.add(value);
            }
        }
        return maxElems;
    }

    @SuppressWarnings("serial")
    public static class EncodeFailureException extends Exception {
        public EncodeFailureException() {
        }

        ;
    }

    @SuppressWarnings("serial")
    public static class DecodeFailureException extends Exception {
        public DecodeFailureException() {
        }

        ;
    }
}