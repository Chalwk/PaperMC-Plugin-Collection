// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.commands;

import com.chalwk.AdminChat;
import com.chalwk.config.PluginConfig;
import com.chalwk.util.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AdminChatCommand implements TabExecutor {

    private final AdminChat plugin;
    private final MessageHelper messageHelper;

    public AdminChatCommand(AdminChat plugin, MessageHelper messageHelper) {
        this.plugin = plugin;
        this.messageHelper = messageHelper;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {

        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!sender.hasPermission("adminchat.use")) {
            messageHelper.sendMessage(sender, config.getNoPermissionMsg());
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                messageHelper.sendMessage(sender, config.getPlayersOnlyMsg());
                return true;
            }

            if (plugin.getChatManager().hasToggledChannel(player)) {
                String channel = plugin.getChatManager().getToggledChannel(player);
                messageHelper.sendMessage(sender, config.getUsageToggledMsg()
                        .replace("{channel}", channel));
            } else {
                messageHelper.sendMessage(sender, config.getUsageMsg());
            }
            return true;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);

        switch (subCommand) {
            case "help":
                sendHelp(sender);
                return true;

            case "reload":
                if (!sender.hasPermission("adminchat.admin")) {
                    messageHelper.sendMessage(sender, config.getNoPermissionMsg());
                    return true;
                }
                plugin.reload();
                messageHelper.sendMessage(sender, config.getReloadedMsg());
                return true;

            case "toggle":
                handleToggle(sender, args);
                return true;

            case "off":
                handleOff(sender);
                return true;
        }

        handleMessage(sender, args);
        return true;
    }

    private void handleToggle(CommandSender sender, String[] args) {
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!(sender instanceof Player player)) {
            messageHelper.sendMessage(sender, config.getPlayersOnlyMsg());
            return;
        }

        if (args.length > 1 && sender.hasPermission("adminchat.admin")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                messageHelper.sendMessage(sender, config.getPlayerNotFoundMsg());
                return;
            }
            plugin.getChatManager().toggleVisibilityForPlayer(target, player);
        } else {
            plugin.getChatManager().toggleVisibility(player);
        }
    }

    private void handleOff(CommandSender sender) {
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!(sender instanceof Player player)) {
            messageHelper.sendMessage(sender, config.getPlayersOnlyMsg());
            return;
        }

        if (plugin.getChatManager().hasToggledChannel(player)) {
            String channel = plugin.getChatManager().getToggledChannel(player);
            plugin.getChatManager().clearToggledChannel(player);
            messageHelper.sendMessage(sender, config.getChannelOffMsg()
                    .replace("{channel}", channel));
        } else {
            messageHelper.sendMessage(sender, config.getNoToggledChannelMsg());
        }
    }

    private void handleMessage(CommandSender sender, String[] args) {
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!(sender instanceof Player player)) {
            messageHelper.sendMessage(sender, config.getPlayersOnlyMsg());
            return;
        }

        String channel;
        String message;

        if (config.channelExists(args[0].toLowerCase(Locale.ROOT))) {
            channel = args[0].toLowerCase(Locale.ROOT);

            if (args.length == 1) {
                plugin.getChatManager().toggleChannel(player, channel);
                return;
            } else {
                message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            }
        } else {
            if (plugin.getChatManager().hasToggledChannel(player)) {
                channel = plugin.getChatManager().getToggledChannel(player);
            } else {
                channel = config.getDefaultChannel();
            }
            message = String.join(" ", args);
        }

        plugin.getChatManager().sendMessage(player, channel, message);
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
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (args.length == 1) {
            String partial = args[0].toLowerCase(Locale.ROOT);

            for (String channel : config.getChannels().keySet()) {
                if (channel.startsWith(partial) && sender.hasPermission(config.getChannelPermission(channel))) {
                    completions.add(channel);
                }
            }

            if ("help".startsWith(partial)) {
                completions.add("help");
            }
            if ("toggle".startsWith(partial)) {
                completions.add("toggle");
            }
            if ("off".startsWith(partial)) {
                completions.add("off");
            }
            if ("reload".startsWith(partial) && sender.hasPermission("adminchat.admin")) {
                completions.add("reload");
            }

            if (sender.hasPermission("adminchat.admin")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase(Locale.ROOT).startsWith(partial)) {
                        completions.add(player.getName());
                    }
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            String partial = args[1].toLowerCase(Locale.ROOT);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase(Locale.ROOT).startsWith(partial)) {
                    completions.add(player.getName());
                }
            }
        }

        return completions;
    }
}