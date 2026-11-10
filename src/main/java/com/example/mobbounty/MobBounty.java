package com.example.mobbounty;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
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
        // Save default config.yml if non-existent
        saveDefaultConfig();

        // Defer the economy lookup until every plugin has finished enabling.
        // Economy providers (EssentialsX, etc.) may register their service after
        // this plugin's onEnable runs, which would make the lookup fail.
        getServer().getScheduler().runTask(this, () -> {
            if (!setupEconomy()) {
                getLogger().severe("Vault or an Economy plugin was not found! Disabling plugin.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            getServer().getPluginManager().registerEvents(this, this);
            getLogger().info("MobBounty has been enabled!");
        });
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

        if (killer == null || entity instanceof Player || entity instanceof ArmorStand) {
            return;
        }

        double reward;

        if (entity.getType() == EntityType.ENDER_DRAGON) {
            reward = getConfig().getDouble("rewards.ender-dragon", 10000.00);
        } else if (isBossMob(entity.getType())) {
            reward = getConfig().getDouble("rewards.boss", 50.00);
        } else {
            reward = getConfig().getDouble("rewards.default", 0.50);
        }

        if (reward > 0) {
            econ.depositPlayer(killer, reward);
            
            String formattedAmount = String.format("%.2f", reward);
            String messageTemplate = getConfig().getString("messages.kill-reward", "&a+$%amount% for killing a %mob%!");
            
            String message = messageTemplate
                    .replace("%amount%", formattedAmount)
                    .replace("%mob%", formatName(entity.getType()));

            killer.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
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
