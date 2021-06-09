package com.lkw657.dynamiccraft;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.ArmorStand;
import org.bukkit.World;
import java.util.HashMap;
import java.util.ArrayList;
import java.sql.*;
import java.util.logging.Level;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.Action;

public class DynamicGateManager implements Listener, Runnable {

    static int searchRadius = 5;
    HashMap<Location, DynamicGate> gates;

    PreparedStatement saveGate;
    PreparedStatement loadGates;
    PreparedStatement deleteGate;

    public boolean createDatabase() {
        DatabaseMetaData dbMeta = null;
        ResultSet tables = null;
        Statement createStatement = null;

        boolean success = true;
        String error = "";
        try {
            error = "DynamicGate: failed to find table";
            dbMeta = DynamicCraft.instance.db.getMetaData();
            tables = dbMeta.getTables(null, null, "DynamicGates", null);

            // Table doesn't exist, create it
            error = "DynamicGate: failed to create table";
            if (! tables.next()) {
                DynamicCraft.instance.logger.log(Level.INFO, "Can't find gate table - creating");
                createStatement = DynamicCraft.instance.db.createStatement();
                createStatement.executeUpdate("CREATE TABLE DynamicGates ("
                + "world string, signX integer, signY integer, signZ integer,"
                + "boundsWorld string, boundsMinX int, boundsMinY int, boundsMinZ int, boundsMaxX int, boundsMaxY int, boundsMaxZ int,"
                + "gateState int)");
            }
        }
        catch (Exception e) {
            DynamicCraft.instance.logger.log(Level.SEVERE, error);
            Utils.logException(e);
            success = false;
        }
        finally {
            try { if(tables != null) tables.close(); } catch (Exception e) {}
            try { if(createStatement != null) createStatement.close(); } catch (Exception e) {}
        }
        return success;
    }

    public boolean loadDatabase() {
        ResultSet tables = null;
        ResultSet dbGates = null;

        boolean success = true;
        String error = "";
        try {
            error = "DynamicGate: failed to load gates";
            dbGates = loadGates.executeQuery();
            while (dbGates.next()) {
                World world = Bukkit.getWorld(dbGates.getString(1));
                int x = dbGates.getInt(2);
                int y = dbGates.getInt(3);
                int z = dbGates.getInt(4);
                Location sign = new Location(world, x, y ,z);
                DynamicGate gate = new DynamicGate();
                gate.readSQL(dbGates, 5);
                gates.put(sign, gate);
            }
        }
        catch (Exception e) {
            DynamicCraft.instance.logger.log(Level.SEVERE, error);
            Utils.logException(e);
            success = false;
        }
        finally {
            try { if(dbGates != null) dbGates.close(); } catch (Exception e) {}
        }
        return success;
    }

    public void register() {
        gates = new HashMap<Location, DynamicGate>();
        if (!createDatabase()) return;
        try {
            saveGate = DynamicCraft.instance.db.prepareStatement("insert into DynamicGates values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            loadGates = DynamicCraft.instance.db.prepareStatement("select * from DynamicGates");
            deleteGate = DynamicCraft.instance.db.prepareStatement("delete from DynamicGates where world = ? and signX = ? and signY = ? and signZ = ?");
        }
        catch (Exception e) {
            DynamicCraft.instance.logger.log(Level.SEVERE, "DynamicGate: Failed to create sql statements");
            Utils.logException(e);
            return;
        }
        if (!loadDatabase()) return;

        Bukkit.getPluginManager().registerEvents(this, DynamicCraft.instance);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DynamicCraft.instance, this, 0, 1);
    }

    public void createGate(Location start, Player player) {
        BlockRect blocks = WorldUtils.findRect(start, Material.IRON_BARS, searchRadius);
        if (blocks == null) {
            player.sendMessage("Failed to find gate");
            return;
        }
        DynamicGate gate = new DynamicGate(blocks);
        gates.put(start, gate);

        try {
            saveGate.setString(1, start.getWorld().getName());
            saveGate.setInt(2, start.getBlockX());
            saveGate.setInt(3, start.getBlockY());
            saveGate.setInt(4, start.getBlockZ());
            gate.writeSQL(saveGate, 5);
            saveGate.executeUpdate();
        }
        catch (Exception e) {
            DynamicCraft.instance.logger.log(Level.SEVERE, "Failed to save gate");
            Utils.logException(e);
        }

    }

    @Override
    public void run() {
        for (DynamicGate entry : gates.values()) {
            entry.run();
        }
    }

    /*
    @EventHandler()
    public void onChunkLoad(ChunkLoadEvent event) {
        // TODO find gate signs in chunk and load other chunks for gate

    }*/

    /*
    @EventHandler()
    public void onChunkUnload(ChunkUnloadEvent event) {

    }
    */

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock() == null) return;
        if (! (event.getBlock().getState() instanceof Sign)) return;
        Sign sign = (Sign) event.getBlock().getState();
        if (!sign.getLine(1).equalsIgnoreCase("{Gate}")) return;

        Player player = event.getPlayer();
        if (!player.hasPermission("dynamiccraft.gate")) {
            player.sendMessage("You do not have permission to destroy a gate");
            event.setCancelled(true);
            return;
        }

        Location signLocation = sign.getLocation();
        DynamicGate gate = gates.get(signLocation);
        if (gate == null) {
            event.getPlayer().sendMessage("Gate not in list");
            return;
        }

        gate.destroy();
        gates.remove(signLocation);

        try {
            deleteGate.setString(1, signLocation.getWorld().getName());
            deleteGate.setInt(2, signLocation.getBlockX());
            deleteGate.setInt(3, signLocation.getBlockY());
            deleteGate.setInt(4, signLocation.getBlockZ());
            deleteGate.executeUpdate();
        }
        catch(Exception e) {
            DynamicCraft.instance.logger.log(Level.SEVERE, "Failed to delete gate");
            Utils.logException(e);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        Sign sign = (Sign) event.getBlock().getState();
        if (!event.getLine(1).equalsIgnoreCase("{Gate}")) return;

        Player player = event.getPlayer();
        if (!player.hasPermission("dynamiccraft.gate")) {
            player.sendMessage("You do not have permission to create a gate");
            event.setCancelled(true);
            return;
        }

        createGate(sign.getLocation(), player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignActivate(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (! (event.getClickedBlock().getState() instanceof Sign)) return;
        Sign sign = (Sign) event.getClickedBlock().getState();
        if (!sign.getLine(1).equalsIgnoreCase("{Gate}")) return;

        Player player = event.getPlayer();
        if (!player.hasPermission("dynamiccraft.gate.use")) {
            player.sendMessage("You do not have permission to use a gate");
            event.setCancelled(true);
            return;
        }

        DynamicGate gate = gates.get(sign.getLocation());
        if (gate == null) {
            event.getPlayer().sendMessage("Gate not in list");
            return;
        }

        if (gate.getState() == GateState.stopped) {
            gate.activate(event.getPlayer());
        }
        else {
            event.getPlayer().sendMessage("Gate is moving");
            return;
        }
    }
}
