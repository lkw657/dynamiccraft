package com.lkw657.dynamiccraft;

import org.bukkit.entity.ArmorStand;
import org.bukkit.util.Consumer;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.bukkit.util.EulerAngle;
import org.bukkit.Bukkit;

public class SetArmourStand implements Consumer<ArmorStand> {

    float yaw;
    public SetArmourStand(float inYaw) {
        yaw = inYaw;
    }

    public void accept(ArmorStand stand) {
        stand.setGravity(false);
        stand.setRotation(yaw,0);
        stand.setVisible(false);
        stand.setRightArmPose(new EulerAngle(0,0,0));
        
        ItemStack pickaxe = new ItemStack(Material.STONE_PICKAXE, 1);
        ItemMeta meta = pickaxe.getItemMeta();
        meta.setCustomModelData(1);
        pickaxe.setItemMeta(meta);

        stand.setItem(EquipmentSlot.HAND, pickaxe);
    }

}