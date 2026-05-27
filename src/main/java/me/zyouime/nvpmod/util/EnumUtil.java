package me.zyouime.nvpmod.util;

import com.google.common.base.Enums;

public class EnumUtil {

    public static <T extends Enum<T>> T fromString(Class<T> enumType, String string) {
        return Enums.getIfPresent(enumType, string.toUpperCase()).orNull();
    }
}