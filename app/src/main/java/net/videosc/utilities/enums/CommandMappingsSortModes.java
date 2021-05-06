package net.videosc.utilities.enums;

import java.util.Arrays;

public enum CommandMappingsSortModes {
    SORT_BY_COLOR,
    SORT_BY_NUM;

    public static String[] getNames(Class<? extends Enum<?>> e) {
        return Arrays.toString(e.getEnumConstants()).replaceAll("^.|.$", "").split(", ");
    }
}
