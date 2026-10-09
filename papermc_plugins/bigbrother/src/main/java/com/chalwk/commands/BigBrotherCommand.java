// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.commands;

import com.chalwk.BigBrother;
import com.chalwk.config.PluginConfig;
import com.chalwk.util.MessageHelper;
import com.chalwk.util.SpyType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BigBrotherCommand implements TabExecutor {

    private final BigBrother plugin;

    public BigBrotherCommand(BigBrother plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {

        if (!sender.hasPermission("bigbrother.use")) {
            MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getNoPermissionMsg());
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getPlayersOnlyMsg());
                return true;
            }

            boolean newState = plugin.getSpyManager().toggleGlobal(player);
            PluginConfig config = plugin.getConfigManager().getConfig();
            String message = newState ? config.getGlobalEnabledMsg() : config.getGlobalDisabledMsg();
            MessageHelper.sendMessage(sender, message);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelp(sender);
                return true;

            case "reload":
                if (!sender.hasPermission("bigbrother.reload")) {
                    MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getNoPermissionMsg());
                    return true;
                }
                plugin.reload();
                MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getReloadedMsg());
                return true;

            case "status":
                if (!(sender instanceof Player player)) {
                    MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getPlayersOnlyMsg());
                    return true;
                }
                MessageHelper.sendMessage(sender, plugin.getSpyManager().getStatusMessage(player));
                return true;

            default:
                SpyType spyType = SpyType.fromCommand(subCommand);
                if (spyType != null) {
                    handleSpyToggle(sender, args, spyType);
                    return true;
                }

                MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getUnknownCommandMsg());
                return true;
        }
    }

    private void handleSpyToggle(CommandSender sender, String[] args, SpyType spyType) {
        if (!(sender instanceof Player player)) {
            MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getPlayersOnlyMsg());
            return;
        }

        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!player.hasPermission(spyType.getPermission())) {
            MessageHelper.sendMessage(sender, config.getNoPermissionMsg());
            return;
        }

        if (args.length > 1) {
            if (!player.hasPermission(spyType.getPermissionOthers())) {
                MessageHelper.sendMessage(sender, config.getNoPermissionMsg());
                return;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                MessageHelper.sendMessage(sender, config.getPlayerNotFoundMsg());
                return;
            }

            plugin.getSpyManager().toggleSpyForPlayer(target, spyType);
            boolean enabled = plugin.getSpyManager().getEnabledSpies(target).contains(spyType);
            String state = enabled ? "enabled" : "disabled";
            MessageHelper.sendMessage(sender,
                    config.getSpyToggledOtherMsg()
                            .replace("{spy}", spyType.getConfigKey())
                            .replace("{state}", state)
                            .replace("{player}", target.getName()));
        } else {
            plugin.getSpyManager().toggleSpy(player, spyType);
            boolean enabled = plugin.getSpyManager().getEnabledSpies(player).contains(spyType);
            String state = enabled ? "enabled" : "disabled";
            MessageHelper.sendMessage(sender,
                    config.getSpyToggledSelfMsg()
                            .replace("{spy}", spyType.getConfigKey())
                            .replace("{state}", state));
        }
    }

    private void sendHelp(CommandSender sender) {
        MessageHelper.sendMessage(sender, "&6&lBigBrother Help");
        MessageHelper.sendMessage(sender, "&e/bigbrother &7- Toggle all spy features");
        MessageHelper.sendMessage(sender, "&e/bigbrother status &7- Check your spy status");

        for (SpyType type : SpyType.values()) {
            MessageHelper.sendMessage(sender,
                    "&e/bigbrother " + type.getCommand() + " [player] &7- Toggle " + type.getConfigKey() + " spy");
        }

        MessageHelper.sendMessage(sender, "&e/bigbrother reload &7- Reload configuration");
        MessageHelper.sendMessage(sender, "&e/bigbrother help &7- Show this help");
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
            options.add("status");
            if (sender.hasPermission("bigbrother.reload")) {
                options.add("reload");
            }
            for (SpyType type : SpyType.values()) {
                if (sender.hasPermission(type.getPermission())) {
                    options.add(type.getCommand());
                }
            }

            for (String opt : options) {
                if (opt.startsWith(partial)) {
                    completions.add(opt);
                }
            }
        } else if (args.length == 2) {
            SpyType spyType = SpyType.fromCommand(args[0]);
            if (spyType != null && sender.hasPermission(spyType.getPermissionOthers())) {
                String partial = args[1].toLowerCase();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(partial)) {
                        completions.add(player.getName());
                    }
                }
            }
        }

        return completions;
    }
}