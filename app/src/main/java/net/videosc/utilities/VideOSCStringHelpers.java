package net.videosc.utilities;

import android.util.Log;

public class VideOSCStringHelpers {
    final private static String TAG = VideOSCStringHelpers.class.getSimpleName();

    public static String padMappingsString(String inputString, int size, Character filler) {
        String res;
        int inputLength = inputString.length() / 3;
        if (inputLength > size) {
            String substrR = inputString.substring(0, inputLength);
            String substrG = inputString.substring(inputLength, inputLength * 2);
            String substrB = inputString.substring(inputLength * 2, inputLength * 3);
            Log.d(TAG, "red substr: " + substrR + "\ngreen substr: " + substrG + "\nblue substr: " + substrB);
            substrR = substrR.substring(0, size);
            substrG = substrG.substring(0, size);
            substrB = substrB.substring(0, size);
            res = substrR.concat(substrG.concat(substrB));
        } else if (inputLength < size) {
            final int padLength = size - inputLength;
            final StringBuilder builderR = new StringBuilder(inputString.substring(0, inputLength));
            final StringBuilder builderG = new StringBuilder(inputString.substring(inputLength, inputLength * 2));
            final StringBuilder builderB = new StringBuilder(inputString.substring(inputLength * 2, inputLength * 3));
            for (int i = 0; i < padLength; i++) {
                builderR.append(filler);
                builderG.append(filler);
                builderB.append(filler);
            }
            res = String.valueOf(builderR).concat(String.valueOf(builderG).concat(String.valueOf(builderB)));
        } else {
            res = inputString;
        }

        return res;
    }
}
