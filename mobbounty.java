package com.example.mobbounty;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class MobBounty extends JavaPlugin implements Listener {

    private Economy econ = null;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault or an Economy plugin was not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getPluginManager().registerEvents(this, getServer().getPluginManager().isPluginEnabled(this) ? this : null);
        getLogger().info("MobBounty has been enabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        // Check if killed by a player
        if (killer == null) {
            return;
        }

        double reward = 0.0;

        // Ender Dragon
        if (entity.getType() == EntityType.ENDER_DRAGON) {
            reward = 10000.00;
        } 
        // Boss mobs (Wither, Warden, Elder Guardian)
        else if (isBossMob(entity.getType())) {
            reward = 50.00;
        } 
        // Regular mobs
        else {
            reward = 0.50;
        }

        if (reward > 0) {
            econ.depositPlayer(killer, reward);
            killer.sendMessage(ChatColor.GREEN + "+$" + String.format("%.2f", reward) + " for killing a " + formatName(entity.getType()));
        }
    }

    private boolean isBossMob(EntityType type) {
        return type == EntityType.WITHER || 
               type == EntityType.WARDEN || 
               type == EntityType.ELDER_GUARDIAN;
    }

    private String formatName(EntityType type) {
        String[] words = type.name().toLowerCase().split("_");
        StringBuilder name = new StringBuilder();
        for (String word : words) {
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return name.toString().trim();
    }
}
