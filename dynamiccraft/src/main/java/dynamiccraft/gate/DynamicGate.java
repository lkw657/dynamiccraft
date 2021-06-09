package com.lkw657.dynamiccraft;
import java.util.HashMap;
import java.util.ArrayList;
import org.bukkit.entity.Player;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.Vector;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import java.sql.*;
import org.bukkit.Location;
import java.util.Collection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import java.util.logging.Level;

class DynamicGate {
    BlockRect bounds;
    GateState state;
    float yaw;
    
    double topRow; // world y coord
    int rowSize;
    int rows;
    ArrayList<ArmorStand> armourStands;
    static Vector velocity = new Vector(0, 0.02, 0);

    // Whether the chunk is loaded
    // when the server starts and the gate is loaded the chunk won't be and it can't find armour stands
    // Need to work out what happens to the armour stands when the chunk unloads
    boolean loaded = false;


    public DynamicGate() {}

    public void load() {
        // TODO check all gate chunks loaded
        Location centre = bounds.min.clone();
        centre.add(bounds.max);
        centre.multiply(0.5);

        Collection<LivingEntity> entities = bounds.min.getWorld().getNearbyLivingEntities(centre, bounds.lengthX()*0.5, bounds.lengthY()*0.5, bounds.lengthZ()*0.5);
        armourStands = new ArrayList<ArmorStand>();
        for (Entity e : entities) {
            if (e.getType() == EntityType.ARMOR_STAND) armourStands.add((ArmorStand) e);
        }
        armourStands.sort((a,b) -> {return Double.compare(a.getLocation().getY(),b.getLocation().getY());});
        loaded = true;
    }

    public int readSQL(ResultSet results, int index) throws SQLException {
        bounds = new BlockRect();
        index = bounds.readSQL(results, index);
        state = GateState.values()[results.getInt(index++)];

        topRow = bounds.max.getBlockY();
        rows = bounds.blockLengthY();
        rowSize = bounds.blockLengthX() * bounds.blockLengthZ();
        double lengthX = bounds.lengthX();
        double lengthZ = bounds.lengthZ();
        if (lengthX < lengthZ) {
            yaw = 90f;
        }
        else if (lengthZ < lengthX) {
            yaw = 0;
        }

        loaded = false;

        return index;
    }

    public int writeSQL(PreparedStatement stmt, int index) throws SQLException {
        index = bounds.writeSQL(stmt, index);
        stmt.setInt(index, state.value);
        //stream.writeFloat(yaw);
        return index;
    }

    public DynamicGate(BlockRect inBounds) {
        bounds = inBounds;
        double lengthX = bounds.lengthX();
        double lengthZ = bounds.lengthZ();

        // Stands seem to point on Z axis by default
        // i.e. if Z length is shorter want yaw of 0
        if (lengthX < lengthZ) {
            yaw = 90f;
        }
        else if (lengthZ < lengthX) {
            yaw = 0;
        }

        armourStands = (ArrayList<ArmorStand>) WorldUtils.spawnEntities(bounds, ArmorStand.class, new SetArmourStand(yaw));
        WorldUtils.replaceBlocks(Material.BARRIER, bounds);
        //player.sendMessage(String.format("Created gate with %d blocks", armourStands.size()));
        // Sort by height - lowest first
        armourStands.sort((a,b) -> {return Double.compare(a.getLocation().getY(),b.getLocation().getY());});
        
        topRow = bounds.max.getBlockY();
        rows = bounds.blockLengthY();
        rowSize = bounds.blockLengthX() * bounds.blockLengthZ();
        state = GateState.stopped;
        loaded = true;

    }

    public GateState getState() {
        return state;
    }

    public void activate(Player player) {
        if (!loaded) load();

        if (armourStands.size() != rowSize) {
            player.sendMessage("Opening gate");
            open();
        }
        else {
            player.sendMessage("Closing gate");
            WorldUtils.replaceBlocks(Material.BARRIER, bounds);
            close();
        }
    }

    public void run() {
        switch (state) {
            case stopped:
                return;
            case opening:
                open();
                break;
            case closing:
                close();
                break;
        }
    }

    public void open() {
        state = GateState.opening;
        if (armourStands.size() == rowSize) {
            WorldUtils.replaceBlocks(Material.AIR, bounds);
            state = GateState.stopped;
            return;
        }
        
        for (ArmorStand stand : armourStands) {
            stand.teleport(stand.getLocation().add(velocity));
        }

        if (armourStands.get(armourStands.size()-1).getLocation().getBlockY() >= topRow+1) {
            int size = armourStands.size(); // changes during loop
            for (int i=size-1; i>= size-rowSize; i--) {
                armourStands.get(i).remove();
                // Hopefully removing the end element is O(1), docs don't seem to say
                // Probably should switch to a stack
                armourStands.remove(i);
            }
        }
    }

    public void close() {
        state = GateState.closing;

        // We need to see if the bottom row is about to enter the next block, not if it has
        // Probably should just check how far the stand is from the centre of the block
        Location bottom = null;
        if (armourStands.size() != 0) {
            bottom = armourStands.get(armourStands.size()-1).getLocation();
            bottom.subtract(velocity);
        }

        if (armourStands.size() == 0 || bottom.getBlockY() <=  topRow-1) {
            if (armourStands.size() == rowSize*rows) {
                state = GateState.stopped;
                return;
            }

            BlockRect topBounds = bounds.clone();
            topBounds.max.setY(topBounds.max.getY()+1);
            topBounds.min.setY(topBounds.max.getY());
            ArrayList<ArmorStand> newStands = WorldUtils.spawnEntities(topBounds, ArmorStand.class, new SetArmourStand(yaw));
            armourStands.addAll(newStands);
        }

        for (ArmorStand stand : armourStands) {  
            stand.teleport(stand.getLocation().subtract(velocity));
        }

    }

    public void destroy() {
        if (!loaded) load();
        WorldUtils.replaceBlocks(Material.IRON_BARS, bounds);
        for (ArmorStand as : armourStands) {
            as.remove();
        }
    }
}