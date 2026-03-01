package com.stormai.plugin;

import org.bukkit.plugin.java.JavaPlugin;

public class FireMcMace extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("FireMcMace plugin has been enabled!");
        getCommand("firemc").setExecutor(new CommandHandler(this));
        getServer().getPluginManager().registerEvents(new MazeListener(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("FireMcMace plugin has been disabled!");
    }
}