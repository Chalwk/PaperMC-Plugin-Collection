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
            MessageHelper.sendMessage(sender, "&6/gmmanage reload &7- Reload the configuration");
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

        MessageHelper.sendMessage(sender, "&cUnknown subcommand. Use &6/gmmanage help");
        return true;
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