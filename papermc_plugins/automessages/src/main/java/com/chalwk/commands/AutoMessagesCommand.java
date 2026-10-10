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
import java.util.Locale;

/**
 * Handles {@code /automessages} and its subcommands.
 *
 * <p>
 * Subcommand dispatch is a small set of if-checks rather than a switch,
 * because there are only three of them and adding more would be trivial.
 * </p>
 */
public class AutoMessagesCommand implements TabExecutor {

    private final AutoMessages plugin;
    private final MessageHelper messageHelper;

    public AutoMessagesCommand(AutoMessages plugin, MessageHelper messageHelper) {
        this.plugin = plugin;
        this.messageHelper = messageHelper;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {

        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!sender.hasPermission("automessages.use")) {
            messageHelper.sendMessage(sender, config.getNoPermissionMsg());
            return true;
        }

        // Bare /automessages and /automessages help both show the same block.
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("automessages.reload")) {
                messageHelper.sendMessage(sender, config.getNoPermissionMsg());
                return true;
            }
            plugin.reload();
            messageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getReloadedMsg());
            return true;
        }

        if (args[0].equalsIgnoreCase("status")) {
            showStatus(sender);
            return true;
        }

        messageHelper.sendMessage(sender, config.getUnknownCommandMsg());
        return true;
    }

    /**
     * Prints the current scheduler state. The "next" index is 1-based for
     * human reading, which is why there's a modulo + 1 in there.
     */
    private void showStatus(CommandSender sender) {
        PluginConfig config = plugin.getConfigManager().getConfig();
        int interval = config.getInterval();
        int size = config.getBroadcasts().size();
        int index = plugin.getMessageScheduler().getCurrentIndex();

        messageHelper.sendMessage(sender, config.getStatusHeaderMsg());
        messageHelper.sendMessage(sender, config.getStatusIntervalMsg()
                .replace("{interval}", String.valueOf(interval)));
        messageHelper.sendMessage(sender, config.getStatusTotalMsg()
                .replace("{total}", String.valueOf(size)));

        String next = size > 0 ? String.valueOf(index % size + 1) : "0";
        messageHelper.sendMessage(sender, config.getStatusNextMsg()
                .replace("{next}", next)
                .replace("{total}", String.valueOf(size)));
    }

    /**
     * Help lines come from config so server owners can reword them without
     * rebuilding the plugin.
     */
    private void sendHelp(CommandSender sender) {
        for (String line : plugin.getConfigManager().getConfig().getHelpMsg()) {
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
            options.add("status");
            // Only suggest reload if they can actually use it.
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