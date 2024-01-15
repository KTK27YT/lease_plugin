package me.ktk27.lease;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
public class Lease extends JavaPlugin implements CommandExecutor {

    private FileConfiguration leaseConfig;
    private Map<Player, BukkitRunnable> leases;
    private List<String[]> activeLeases;
    @Override
    public void onEnable() {
        // Create the lease config file if it doesn't exist
        File configFile = new File(getDataFolder(), "leases.yml");
        if (!configFile.exists()) {
            saveResource("leases.yml", false);
        }

        // Load the lease config file
        FileConfiguration leaseConfig = YamlConfiguration.loadConfiguration(configFile);

        // Register the "lease" command and set this class as the executor
        Objects.requireNonNull(getCommand("lease")).setExecutor(this);
        Objects.requireNonNull(getCommand("activelease")).setExecutor(this);
        // Initialize the leases map
        leases = new HashMap<>();
        activeLeases = new ArrayList<>();
    }

    @Override
    public void onDisable() {
        // Cancel all active leases when the plugin is disabled
        for (BukkitRunnable task : leases.values()) {
            task.cancel();
        }
        leases.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("lease")) {
            if (args.length < 3) {
                // Invalid command usage
                sender.sendMessage(ChatColor.RED + "Usage: /lease <lease_name> <target_player> <duration>");
                return true;
            }

            String leaseName = args[0];
            String targetPlayerName = args[1];
            int duration;

            // Check if the player is online
            Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }

            // Check if the player already has an active lease
            if (leases.containsKey(targetPlayer)) {
                sender.sendMessage(ChatColor.RED + "The specified player already has an active lease.");

                return true;
            }

            try {
                duration = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid duration. Please provide a number.");
                return true;
            }

            if (duration <= 0) {
                sender.sendMessage(ChatColor.RED + "Invalid duration. Please provide a positive number.");
                return true;
            }

            // Get the lease configuration section from the plugin.yml
            ConfigurationSection leaseSection = getConfig().getConfigurationSection("leases." + leaseName);
            if (leaseSection == null) {
                sender.sendMessage(ChatColor.RED + "Invalid lease name.");
                return true;
            }

            String startCmd = leaseSection.getString("start_cmd");
            String endCmd = leaseSection.getString("end_cmd");

            if (startCmd == null || endCmd == null) {
                sender.sendMessage(ChatColor.RED + "Invalid lease configuration.");
                return true;
            }

            // Execute the start command
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), replacePlaceholders(startCmd, targetPlayer));

            // Create a BukkitRunnable to execute the end command after the duration
            BukkitRunnable endTask = new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), replacePlaceholders(endCmd, targetPlayer));


                    // Send lease expired message to the target player
                    String expiredMessage = leaseSection.getString("expired_message");
                    if (expiredMessage != null && !expiredMessage.isEmpty()) {
                        String message = replacePlaceholders(expiredMessage, targetPlayer);
                        targetPlayer.sendMessage(ChatColor.RED + message);
                    }
                    // Remove the entries associated with the player from activeLeases list
                    List<String[]> leasesToRemove = new ArrayList<>();
                    for (String[] lease : activeLeases) {
                        if (lease[0].equals(targetPlayer.getName())) {
                            leasesToRemove.add(lease);
                        }
                    }
                    activeLeases.removeAll(leasesToRemove);
                    leases.remove(targetPlayer);
                    targetPlayer.sendMessage(ChatColor.RED + " your " + leaseName + " lease has expired");
                }
            };

            // Schedule the end task to run after the specified duration
            endTask.runTaskLater(this, duration * 20 * 60); // Convert minutes to ticks

            // Add the lease to the map
            leases.put(targetPlayer, endTask);
            String[] leaseInfo = {targetPlayer.getName(), leaseName, String.valueOf(duration)};
            activeLeases.add(leaseInfo);
            // Send lease granted message to the target player
            String grantMessage = leaseSection.getString("grant_message");
            if (grantMessage != null && !grantMessage.isEmpty()) {
                String message = replacePlaceholders(grantMessage, targetPlayer);
                targetPlayer.sendMessage(ChatColor.GREEN + message);
            }

            targetPlayer.sendMessage(ChatColor.GREEN + leaseName + " lease granted to you successfully for " + duration + " minute");
            return true;
        } else if (command.getName().equalsIgnoreCase("activelease")) {
            // Code for the activelease command
            if (!sender.hasPermission("lease.activelease")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }

            sender.sendMessage(ChatColor.GREEN + "Active Leases:");
            for (String[] lease : activeLeases) {
                sender.sendMessage(ChatColor.YELLOW + lease[0] + ": " +
                        ChatColor.WHITE + lease[1] + " (Duration: " + lease[2] + " minute)");
            }


            return true;
        }

        return false;
    }






    private String replacePlaceholders (String command, Player player){
            return command
                    .replace("%player%", player.getName())
                    .replace("%person_who_initiated_the_command%", player.getName())
                    .replace("%lease_name%", command);
        }
    }
