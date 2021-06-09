package com.lkw657.dynamiccraft;
import java.util.logging.Level;
import org.bukkit.plugin.PluginLogger;

public class Utils {

    static void logException(Exception e) {
        DynamicCraft.instance.logger.log(Level.SEVERE, e.toString());
        for (StackTraceElement el : e.getStackTrace()) {
            DynamicCraft.instance.logger.log(Level.SEVERE, el.toString());
        }
    }

}