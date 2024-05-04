package com.darkona.adventurebackpack.util;

import java.lang.reflect.Field;

import com.darkona.adventurebackpack.reference.LoadedMods;

public class HungerOverhaulUtils {

    private HungerOverhaulUtils() {}

    private static int milkedTimeout;

    static {
        if (LoadedMods.HUNGEROVERHAUL) {
            readMilkedTimeout();
        }
    }

    private static void readMilkedTimeout() {
        if (Utils.inClient()) {
            try {
                Class<?> iguanaConfigClass = Class.forName("iguanaman.hungeroverhaul.config.IguanaConfig");
                Field milkedTimeoutField = iguanaConfigClass.getDeclaredField("milkedTimeout");
                milkedTimeout = milkedTimeoutField.getInt(null);
            } catch (Exception e) {
                LogHelper.error("Error getting instance of HungerOverhaul Config: " + e);
            }
        }
    }

    public static int getMilkedTimeout() {
        return milkedTimeout;
    }
}
