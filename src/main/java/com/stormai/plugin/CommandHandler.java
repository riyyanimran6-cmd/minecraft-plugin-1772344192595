package com.stormai.plugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {
    private final FireMcMace plugin;

    public CommandHandler(FireMcMace plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("firemc")) {
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.GOLD + "=== FireMcMace Commands ===");
            player.sendMessage(ChatColor.AQUA + "/firemc help - Show this help menu");
            player.sendMessage(ChatColor.AQUA + "/firemc reload - Reload the plugin configuration");
            player.sendMessage(ChatColor.AQUA + "/firemc start - Start the Fire Maze challenge");
            player.sendMessage(ChatColor.AQUA + "/firemc stop - Stop the Fire Maze challenge");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                player.sendMessage(ChatColor.GOLD + "=== FireMcMace Help ===");
                player.sendMessage(ChatColor.AQUA + "/firemc help - Show this help menu");
                player.sendMessage(ChatColor.AQUA + "/firemc reload - Reload the plugin configuration");
                player.sendMessage(ChatColor.AQUA + "/firemc start - Start the Fire Maze challenge");
                player.sendMessage(ChatColor.AQUA + "/firemc stop - Stop the Fire Maze challenge");
                break;

            case "reload":
                plugin.reloadConfig();
                player.sendMessage(ChatColor.GREEN + "FireMcMace configuration reloaded!");
                break;

            case "start":
                player.sendMessage(ChatColor.GREEN + "Fire Maze challenge started!");
                // TODO: Implement start logic
                break;

            case "stop":
                player.sendMessage(ChatColor.RED + "Fire Maze challenge stopped!");
                // TODO: Implement stop logic
                break;

            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /firemc help for a list of commands.");
                return true;
        }

        return true;
    }
}