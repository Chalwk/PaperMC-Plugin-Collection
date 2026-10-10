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
    private final MessageHelper messageHelper;

    public GameModeCommand(GameModeManager plugin, MessageHelper messageHelper) {
        this.plugin = plugin;
        this.messageHelper = messageHelper;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {

        if (!sender.hasPermission("gmmanage.use")) {
            messageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getNoPermissionMsg());
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            for (String line : plugin.getConfigManager().getConfig().getHelpLines()) {
                messageHelper.sendMessage(sender, line);
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("gmmanage.reload")) {
                messageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getNoPermissionMsg());
                return true;
            }
            plugin.reload();
            messageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getReloadedMsg());
            return true;
        }

        messageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getUnknownSubcommandMsg());
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