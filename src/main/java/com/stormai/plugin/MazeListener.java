package com.stormai.plugin;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MazeListener implements Listener {
    private final FireMcMace plugin;
    private final Map<UUID, Long> lastDamageTime = new HashMap<>();
    private final Set<Location> lavaTrapLocations = new HashSet<>();
    private final Set<Location> flameWallSections = new HashSet<>();
    private final Map<Location, Integer> mobSpawnZones = new HashMap<>();
    private boolean mazeActive = false;

    public MazeListener(FireMcMace plugin) {
        this.plugin = plugin;
        loadConfig();
        startFlameWallTask();
        startMobSpawnTask();
    }

    private void loadConfig() {
        // Load configuration from config.yml
        plugin.saveDefaultConfig();
        mazeActive = plugin.getConfig().getBoolean("maze-active", false);
    }

    private void startFlameWallTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!mazeActive) return;

                for (Location loc : flameWallSections) {
                    loc.getWorld().spawnParticle(Particle.FLAME, loc, 10, 0.5, 0.5, 0.5, 0.05);
                    loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 5, 0.5, 0.5, 0.5, 0.02);

                    for (Entity entity : loc.getWorld().getNearbyEntities(loc, 1, 1, 1)) {
                        if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                            ((LivingEntity) entity).damage(2.0);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void startMobSpawnTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!mazeActive) return;

                for (Map.Entry<Location, Integer> entry : mobSpawnZones.entrySet()) {
                    Location loc = entry.getKey();
                    int count = entry.getValue();

                    for (int i = 0; i < count; i++) {
                        loc.getWorld().spawnEntity(loc, EntityType.ZOMBIFIED_PIGLIN);
                        loc.getWorld().spawnEntity(loc, EntityType.BLAZE);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 200);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!mazeActive) return;

        Player player = event.getPlayer();
        Location to = event.getTo();

        // Check for heat damage
        if (isInsideMazeArea(to)) {
            long currentTime = System.currentTimeMillis();
            if (!lastDamageTime.containsKey(player.getUniqueId()) ||
                currentTime - lastDamageTime.get(player.getUniqueId()) >= 2000) {
                player.damage(1.0);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                lastDamageTime.put(player.getUniqueId(), currentTime);
            }
        }

        // Check for lava trap triggers
        for (Location trapLoc : lavaTrapLocations) {
            if (trapLoc.distanceSquared(to) < 4) {
                triggerLavaTrap(trapLoc);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!mazeActive) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.STONE_BUTTON) {
                Location loc = block.getLocation();
                if (lavaTrapLocations.contains(loc)) {
                    triggerLavaTrap(loc);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
            event.getCause() == EntityDamageEvent.DamageCause.FIRE ||
            event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                player.sendMessage(ChatColor.RED + "The heat is unbearable!");
            }
        }
    }

    private boolean isInsideMazeArea(Location location) {
        // Simple check for maze area (you can customize this)
        World world = location.getWorld();
        if (world == null) return false;

        return location.getBlockX() >= -100 && location.getBlockX() <= 100 &&
               location.getBlockZ() >= -100 && location.getBlockZ() <= 100;
    }

    private void triggerLavaTrap(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        loc.getWorld().spawnParticle(Particle.LAVA, loc, 20, 1, 1, 1, 0.1);
        loc.getWorld().playSound(loc, Sound.BLOCK_LAVA_POP, 1.0f, 1.0f);

        for (Entity entity : world.getNearbyEntities(loc, 3, 3, 3)) {
            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).damage(3.0);
                entity.setFireTicks(100);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!mazeActive) return;

        Block block = event.getBlock();
        if (block.getType() == Material.NETHERRACK) {
            flameWallSections.add(block.getLocation());
            event.getPlayer().sendMessage(ChatColor.YELLOW + "Added to flame wall section!");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!mazeActive) return;

        Location loc = event.getBlock().getLocation();
        flameWallSections.remove(loc);
        lavaTrapLocations.remove(loc);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!mazeActive) return;

        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity entity = (LivingEntity) event.getEntity();
            if (entity.getType() == EntityType.ZOMBIFIED_PIGLIN ||
                entity.getType() == EntityType.BLAZE) {
                // Give fire resistance to spawned mobs
                entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1));
            }
        }
    }

    public void addLavaTrap(Location loc) {
        lavaTrapLocations.add(loc);
    }

    public void addMobSpawnZone(Location loc, int count) {
        mobSpawnZones.put(loc, count);
    }

    public void setMazeActive(boolean active) {
        this.mazeActive = active;
    }
}