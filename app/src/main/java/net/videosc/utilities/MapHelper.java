package net.videosc.utilities;

import java.util.Map;

public class MapHelper {

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (value == entry.getValue()) {
                return entry.getKey();
            }
        }

        return null;
    }
}
