// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.commands;

import com.chalwk.AutoMessages;
import com.chalwk.config.PluginConfig;
import com.chalwk.util.MessageHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AutoMessagesCommand implements TabExecutor {

    private final AutoMessages plugin;

    public AutoMessagesCommand(AutoMessages plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {

        if (!sender.hasPermission("automessages.use")) {
            MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getNoPermissionMsg());
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("automessages.reload")) {
                MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getNoPermissionMsg());
                return true;
            }
            plugin.reload();
            MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getReloadedMsg());
            return true;
        }

        if (args[0].equalsIgnoreCase("status")) {
            showStatus(sender);
            return true;
        }

        MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getUnknownCommandMsg());
        return true;
    }

    private void showStatus(CommandSender sender) {
        PluginConfig config = plugin.getConfigManager().getConfig();
        int interval = config.getInterval();
        int size = config.getMessages().size();
        int index = plugin.getMessageScheduler().getCurrentIndex();

        MessageHelper.sendMessage(sender, config.getStatusHeaderMsg());
        MessageHelper.sendMessage(sender, config.getStatusIntervalMsg()
                .replace("{interval}", String.valueOf(interval)));
        MessageHelper.sendMessage(sender, config.getStatusTotalMsg()
                .replace("{total}", String.valueOf(size)));

        String next = size > 0 ? String.valueOf(index % size + 1) : "0";
        MessageHelper.sendMessage(sender, config.getStatusNextMsg()
                .replace("{next}", next)
                .replace("{total}", String.valueOf(size)));
    }

    private void sendHelp(CommandSender sender) {
        MessageHelper.sendMessage(sender, "&6&lAutoMessages Help");
        MessageHelper.sendMessage(sender, "&e/automessages status &7- Show current status");
        MessageHelper.sendMessage(sender, "&e/automessages reload &7- Reload configuration and restart scheduler");
        MessageHelper.sendMessage(sender, "&e/automessages help &7- Show this help");
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
            if (sender.hasPermission("automessages.reload")) {
                options.add("reload");
            }

            for (String opt : options) {
                if (opt.startsWith(partial)) {
                    completions.add(opt);
                }
            }
        }

        return completions;
    }
}