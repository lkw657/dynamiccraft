package com.lkw657.dynamiccraft;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.util.Consumer;
import java.util.ArrayList;

public class WorldUtils {

    static <T extends Entity> ArrayList<T> spawnEntities(BlockRect rect, Class<T> entity, Consumer<T> fun) {
        ArrayList<T> entities = new ArrayList<T>();
        World world = rect.min.getWorld();
        Location min = rect.min;
        Location max = rect.max;
        for (int x=min.getBlockX(); x<max.getBlockX()+1; x++)
            for (int y=min.getBlockY(); y<max.getBlockY()+1; y++)
                for (int z=min.getBlockZ(); z<max.getBlockZ()+1; z++)
                    // seems to refer the the minimum of the block, except the y coord seems to be center?
                    entities.add(world.spawn(new Location(world, (double) x + 0.5, (double) y, (double) z + 0.5), entity, fun));
        return entities;
    }

    static void replaceBlocks(Material material, BlockRect rect) {
        replaceBlocks(rect.min.getWorld(), material, rect.min, rect.max);
    }

    static void replaceBlocks(World world, Material material, Location min, Location max) {
        for (int x=min.getBlockX(); x<max.getBlockX()+1; x++)
            for (int y=min.getBlockY(); y<max.getBlockY()+1; y++)
                for (int z=min.getBlockZ(); z<max.getBlockZ()+1; z++)
                    world.getBlockAt(x,y,z).setType(material);
    }

    static Location findLastBlock(Location start, Material material, Location direction) {
        Location curr = start.clone();
        while (curr.getBlock().getType() == material) {
            curr.add(direction);
        }
        curr.subtract(direction);
        return curr;
    }

    static BlockRect findRect(Location startLocation, Material material, int initialRadius) {
        // All this converting int to double is shorter code, but probably a bit slower
        Location found = null;
        for (int x=startLocation.getBlockX()-initialRadius; x<startLocation.getBlockX()+initialRadius; x++) {
            for (int y=startLocation.getBlockY()-initialRadius; y<startLocation.getBlockY()+initialRadius; y++) {
                for (int z=startLocation.getBlockZ()-initialRadius; z<startLocation.getBlockZ()+initialRadius; z++) {
                    Location curr = new Location(startLocation.getWorld(), x, y, z);
                    if (curr.getBlock().getType() == material) {
                        found = curr;
                        break;
                    }
                }
            }
        }
        if (found == null) return null;
        // TODO this assumes the blocks form a rect

        Location min = found.clone();
        Location tmp = findLastBlock(found, material, new Location(found.getWorld(), -1, 0, 0));
        min.setX(tmp.getBlockX());
        tmp = findLastBlock(found, material, new Location(found.getWorld(), 0, -1, 0));
        min.setY(tmp.getBlockY());
        tmp = findLastBlock(found, material, new Location(found.getWorld(), 0, 0, -1));
        min.setZ(tmp.getBlockZ());

        Location max = found.clone();
        tmp = findLastBlock(found, material, new Location(found.getWorld(), 1, 0, 0));
        max.setX(tmp.getBlockX());
        tmp = findLastBlock(found, material, new Location(found.getWorld(), 0, 1, 0));
        max.setY(tmp.getBlockY());
        tmp = findLastBlock(found, material, new Location(found.getWorld(), 0, 0, 1));
        max.setZ(tmp.getBlockZ());

        return new BlockRect(min, max);
    }
}