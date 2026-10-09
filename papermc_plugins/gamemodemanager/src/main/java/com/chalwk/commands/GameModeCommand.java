// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.commands;

import com.chalwk.GameModeManager;
import com.chalwk.util.MessageHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Handles /gmmanage and its subcommands.
 */
public class GameModeCommand implements TabExecutor {
    private final GameModeManager plugin;

    public GameModeCommand(GameModeManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {

        if (!sender.hasPermission("gmmanage.use")) {
            MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getNoPermissionMsg());
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            for (String line : plugin.getConfigManager().getConfig().getHelpLines()) {
                MessageHelper.sendMessage(sender, line);
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("gmmanage.reload")) {
                MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getNoPermissionMsg());
                return true;
            }
            plugin.reload();
            MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getReloadedMsg());
            return true;
        }

        MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getUnknownSubcommandMsg());
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String partial = args[0].toLowerCase(Locale.ROOT);

            // Only suggest subcommands the sender can actually run.
            List<String> options = new ArrayList<>();
            if (sender.hasPermission("gmmanage.reload")) {
                options.add("reload");
            }
            options.add("help");

            for (String opt : options) {
                if (opt.startsWith(partial)) {
                    completions.add(opt);
                }
            }
        }
        return completions;
    }
}