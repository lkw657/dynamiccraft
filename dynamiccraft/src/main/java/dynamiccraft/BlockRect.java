package com.lkw657.dynamiccraft;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.World;
import java.sql.*;

class BlockRect {
    // Locations are inclusive
    public Location min;
    public Location max;

    public BlockRect() {}

    public BlockRect(Location inMin, Location inMax) {
        min = inMin;
        max = inMax;
    }

    public double lengthX() {
        return max.getX() - min.getX() + 1;
    }
    public double lengthY() {
        return max.getY() - min.getY() + 1;
    }
    public double lengthZ() {
        return max.getZ() - min.getZ() + 1;
    }

    public int blockLengthX() {
        return max.getBlockX() - min.getBlockX() + 1;
    }
    public int blockLengthY() {
        return max.getBlockY() - min.getBlockY() + 1;
    }
    public int blockLengthZ() {
        return max.getBlockZ() - min.getBlockZ() + 1;
    }

    public BlockRect clone() {
        return new BlockRect(min.clone(), max.clone());
    }

    // TODO how to handle world doesn't exist
    public int readSQL(ResultSet results, int index) throws SQLException {
        World world = Bukkit.getServer().getWorld(results.getString(index++));
        double x = results.getDouble(index++);
        double y = results.getDouble(index++);
        double z = results.getDouble(index++);
        min = new Location(world, x, y, z);
        x = results.getDouble(index++);
        y = results.getDouble(index++);
        z = results.getDouble(index++);
        max = new Location(world, x, y, z);
        return index;
    }

    public int writeSQL(PreparedStatement stmt, int index) throws SQLException {
        stmt.setString(index++, min.getWorld().getName());
        stmt.setDouble(index++, min.getX());
        stmt.setDouble(index++, min.getY());
        stmt.setDouble(index++, min.getZ());
        stmt.setDouble(index++, max.getX());
        stmt.setDouble(index++, max.getY());
        stmt.setDouble(index++, max.getZ());
        return index;
    }
}