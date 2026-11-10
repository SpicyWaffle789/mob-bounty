package com.example.mobbounty;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public final class MobBounty extends JavaPlugin implements Listener {

    private Economy econ = null;
    private final Map<EntityType, Double> mobRewards = new EnumMap<>(EntityType.class);

    @Override
    public void onEnable() {
        // Save default config.yml if non-existent
        saveDefaultConfig();
        loadMobRewards();

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

    /** Reads rewards.mobs from config.yml into a lookup map. */
    private void loadMobRewards() {
        mobRewards.clear();
        ConfigurationSection section = getConfig().getConfigurationSection("rewards.mobs");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            try {
                EntityType type = EntityType.valueOf(key.toUpperCase(Locale.ROOT));
                mobRewards.put(type, section.getDouble(key));
            } catch (IllegalArgumentException e) {
                getLogger().warning("Unknown mob type in config (rewards.mobs." + key + "), skipping.");
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("mobbounty.reload")) {
                sender.sendMessage(color(getConfig().getString("messages.no-permission", "&cYou don't have permission to do that.")));
                return true;
            }
            reloadConfig();
            loadMobRewards();
            sender.sendMessage(color(getConfig().getString("messages.reloaded", "&aMobBounty config reloaded.")));
            return true;
        }
        sender.sendMessage("Usage: /" + label + " reload");
        return true;
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer == null || entity instanceof Player || entity instanceof ArmorStand) {
            return;
        }

        double reward = getReward(entity.getType());

        if (reward > 0) {
            econ.depositPlayer(killer, reward);

            String formattedAmount = String.format("%.2f", reward);
            String messageTemplate = getConfig().getString("messages.kill-reward", "&a+$%amount% for killing a %mob%!");

            String message = messageTemplate
                    .replace("%amount%", formattedAmount)
                    .replace("%mob%", formatName(entity.getType()));

            killer.sendMessage(color(message));
        }
    }

    /** Per-mob override first, then ender-dragon, boss, and finally the default. */
    private double getReward(EntityType type) {
        Double override = mobRewards.get(type);
        if (override != null) {
            return override;
        }
        if (type == EntityType.ENDER_DRAGON) {
            return getConfig().getDouble("rewards.ender-dragon", 10000.00);
        }
        if (isBossMob(type)) {
            return getConfig().getDouble("rewards.boss", 50.00);
        }
        return getConfig().getDouble("rewards.default", 0.50);
    }

    private boolean isBossMob(EntityType type) {
        return type == EntityType.WITHER ||
               type == EntityType.WARDEN ||
               type == EntityType.ELDER_GUARDIAN;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
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
