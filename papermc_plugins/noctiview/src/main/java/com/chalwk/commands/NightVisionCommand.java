// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.commands;

import com.chalwk.NoctiView;
import com.chalwk.config.PluginConfig;
import com.chalwk.util.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NightVisionCommand implements TabExecutor {

    private final NoctiView plugin;

    public NightVisionCommand(NoctiView plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {

        if (!sender.hasPermission("nightvision.use")) {
            MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getNoPermissionMsg());
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getPlayersOnlyMsg());
                return true;
            }
            plugin.getNightVisionManager().toggleNightVision(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("help")) {
            sendHelp(sender);
            return true;
        }

        if (subCommand.equals("reload")) {
            if (!sender.hasPermission("noctiview.admin")) {
                MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getNoPermissionMsg());
                return true;
            }
            plugin.reload();
            MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getReloadedMsg());
            return true;
        }

        if (subCommand.equals("worlds")) {
            if (!sender.hasPermission("noctiview.admin")) {
                MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getNoPermissionMsg());
                return true;
            }
            listWorlds(sender);
            return true;
        }

        if (subCommand.equals("enableworld") || subCommand.equals("disableworld")) {
            if (!sender.hasPermission("noctiview.admin")) {
                MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getNoPermissionMsg());
                return true;
            }
            if (args.length < 2) {
                String usage = subCommand.equals("enableworld")
                        ? plugin.getConfigManager().getConfig().getUsageEnableWorldMsg()
                        : plugin.getConfigManager().getConfig().getUsageDisableWorldMsg();
                MessageHelper.sendMessage(sender, usage);
                return true;
            }
            updateWorld(sender, args[1], subCommand.equals("enableworld"));
            return true;
        }

        // Default: treat the first argument as a player name (toggle for others)
        if (!sender.hasPermission("noctiview.admin")) {
            MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getNoPermissionMsg());
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getPlayerNotFoundMsg());
            return true;
        }

        plugin.getNightVisionManager().toggleNightVisionForPlayer(target, sender);
        return true;
    }

    private void listWorlds(CommandSender sender) {
        PluginConfig config = plugin.getConfigManager().getConfig();
        MessageHelper.sendMessage(sender, config.getWorldHeaderMsg());

        for (World world : Bukkit.getWorlds()) {
            boolean enabled = config.isWorldEnabled(world.getName());
            String color = enabled ? "&a" : "&c";
            String status = enabled ? "ENABLED" : "DISABLED";
            MessageHelper.sendMessage(sender, color + "- " + world.getName() + ": " + status);
        }
    }

    private void updateWorld(CommandSender sender, String worldName, boolean enabled) {
        PluginConfig config = plugin.getConfigManager().getConfig();

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            MessageHelper.sendMessage(sender, config.getWorldNotFoundMsg());
            return;
        }

        plugin.getConfigManager().setWorldEnabled(worldName, enabled);

        String message = (enabled ? config.getWorldEnabledMsg() : config.getWorldDisabledMsg())
                .replace("{world}", worldName);
        MessageHelper.sendMessage(sender, message);
    }

    private void sendHelp(CommandSender sender) {
        MessageHelper.sendMessage(sender, "&6&lNoctiView Help");
        MessageHelper.sendMessage(sender, "&e/nightvision &7- Toggle your night vision");
        MessageHelper.sendMessage(sender, "&e/nightvision <player> &7- Toggle night vision for another player");
        MessageHelper.sendMessage(sender, "&e/nightvision worlds &7- List per-world settings");
        MessageHelper.sendMessage(sender, "&e/nightvision enableworld <world> &7- Enable night vision in a world");
        MessageHelper.sendMessage(sender, "&e/nightvision disableworld <world> &7- Disable night vision in a world");
        MessageHelper.sendMessage(sender, "&e/nightvision reload &7- Reload the configuration");
        MessageHelper.sendMessage(sender, "&e/nightvision help &7- Show this help");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partial = args[0].toLowerCase();

            List<String> options = new ArrayList<>();
            options.add("help");
            if (sender.hasPermission("noctiview.admin")) {
                options.add("reload");
                options.add("worlds");
                options.add("enableworld");
                options.add("disableworld");
            }

            for (String opt : options) {
                if (opt.startsWith(partial)) {
                    completions.add(opt);
                }
            }

            if (sender.hasPermission("noctiview.admin")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(partial)) {
                        completions.add(player.getName());
                    }
                }
            }
        } else if (args.length == 2
                && (args[0].equalsIgnoreCase("enableworld") || args[0].equalsIgnoreCase("disableworld"))) {
            String partial = args[1].toLowerCase();
            for (World world : Bukkit.getWorlds()) {
                if (world.getName().toLowerCase().startsWith(partial)) {
                    completions.add(world.getName());
                }
            }
        }

        return completions;
    }
}