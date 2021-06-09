package com.lkw657.dynamiccraft;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.Bukkit;
import java.sql.*;
import java.io.File;
import java.util.logging.Level;
import java.util.Map;

public class DynamicCraft extends JavaPlugin {

    public static DynamicCraft instance;
    public static PluginLogger logger;
    public Connection db;
    DynamicGateManager gateManager;


    @Override
    public void onEnable() {
        DynamicCraft.logger = new PluginLogger(this);
        DynamicCraft.instance = this;
        db = null;

        boolean foundBlockT = false;
        boolean foundGateT = false;
        String error = "";
        ResultSet results = null;
        Statement stmt = null;
        DatabaseMetaData dbMeta = null;

        // Can't use typemap with sqlite - sqlite doesn't support structure types

        try {
            error = "Failed to load database";
            if (!getDataFolder().exists()) getDataFolder().mkdir();
            String filename = new File(getDataFolder(), "state.sqlite").getPath();
            logger.log(Level.INFO, String.format("Loading database: %s", filename));
            db = DriverManager.getConnection(String.format("jdbc:sqlite:%s", filename));

            if (db == null) {
                logger.log(Level.SEVERE, "Failed to load database");
                return;
            }

            logger.log(Level.INFO, "Registring managers");
            gateManager = new DynamicGateManager();
            gateManager.register();
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, error);
            Utils.logException(e);
            return;
        }
        finally {
            try { if(results != null) results.close(); } catch (Exception e) {}
            try { if(stmt != null) stmt.close(); } catch (Exception e) {}
        }
    }

    @Override
    public void onDisable() {
        try {
            db.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to close database");
            Utils.logException(e);
        }
        DynamicCraft.instance = null;
        DynamicCraft.logger = null;
    }
}
