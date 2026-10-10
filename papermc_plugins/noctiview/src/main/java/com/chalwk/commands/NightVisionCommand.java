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
import java.util.Locale;

public class NightVisionCommand implements TabExecutor {

    private final NoctiView plugin;
    private final MessageHelper messageHelper;

    public NightVisionCommand(NoctiView plugin, MessageHelper messageHelper) {
        this.plugin = plugin;
        this.messageHelper = messageHelper;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {

        if (!sender.hasPermission("nightvision.use")) {
            messageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getNoPermissionMsg());
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                messageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getPlayersOnlyMsg());
                return true;
            }
            plugin.getNightVisionManager().toggleNightVision(player);
            return true;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);

        if (subCommand.equals("help")) {
            sendHelp(sender);
            return true;
        }

        if (subCommand.equals("reload")) {
            if (!sender.hasPermission("noctiview.admin")) {
                messageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getNoPermissionMsg());
                return true;
            }
            plugin.reload();
            messageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getReloadedMsg());
            return true;
        }

        if (subCommand.equals("worlds")) {
            if (!sender.hasPermission("noctiview.admin")) {
                messageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getNoPermissionMsg());
                return true;
            }
            listWorlds(sender);
            return true;
        }

        if (subCommand.equals("enableworld") || subCommand.equals("disableworld")) {
            if (!sender.hasPermission("noctiview.admin")) {
                messageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getNoPermissionMsg());
                return true;
            }
            if (args.length < 2) {
                String usage = subCommand.equals("enableworld")
                        ? plugin.getConfigManager().getConfig().getUsageEnableWorldMsg()
                        : plugin.getConfigManager().getConfig().getUsageDisableWorldMsg();
                messageHelper.sendMessage(sender, usage);
                return true;
            }
            updateWorld(sender, args[1], subCommand.equals("enableworld"));
            return true;
        }

        if (!sender.hasPermission("noctiview.admin")) {
            messageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getNoPermissionMsg());
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            messageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getPlayerNotFoundMsg());
            return true;
        }

        plugin.getNightVisionManager().toggleNightVisionForPlayer(target, sender);
        return true;
    }

    private void listWorlds(CommandSender sender) {
        PluginConfig config = plugin.getConfigManager().getConfig();
        messageHelper.sendMessage(sender, config.getWorldHeaderMsg());

        for (World world : Bukkit.getWorlds()) {
            boolean enabled = config.isWorldEnabled(world.getName());
            String color = enabled ? "&a" : "&c";
            String status = enabled ? "ENABLED" : "DISABLED";
            messageHelper.sendMessage(sender, color + "- " + world.getName() + ": " + status);
        }
    }

    private void updateWorld(CommandSender sender, String worldName, boolean enabled) {
        PluginConfig config = plugin.getConfigManager().getConfig();

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            messageHelper.sendMessage(sender, config.getWorldNotFoundMsg());
            return;
        }

        plugin.getConfigManager().setWorldEnabled(worldName, enabled);

        String message = (enabled ? config.getWorldEnabledMsg() : config.getWorldDisabledMsg())
                .replace("{world}", worldName);
        messageHelper.sendMessage(sender, message);
    }

    private void sendHelp(CommandSender sender) {
        for (String line : plugin.getConfigManager().getConfig().getHelpLines()) {
            messageHelper.sendMessage(sender, line);
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partial = args[0].toLowerCase(Locale.ROOT);

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
                    if (player.getName().toLowerCase(Locale.ROOT).startsWith(partial)) {
                        completions.add(player.getName());
                    }
                }
            }
        } else if (args.length == 2
                && (args[0].equalsIgnoreCase("enableworld") || args[0].equalsIgnoreCase("disableworld"))) {
            String partial = args[1].toLowerCase(Locale.ROOT);
            for (World world : Bukkit.getWorlds()) {
                if (world.getName().toLowerCase(Locale.ROOT).startsWith(partial)) {
                    completions.add(world.getName());
                }
            }
        }

        return completions;
    }
}