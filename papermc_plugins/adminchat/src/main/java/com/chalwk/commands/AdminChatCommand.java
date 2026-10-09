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

public class AdminChatCommand implements TabExecutor {

    private final AdminChat plugin;

    public AdminChatCommand(AdminChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {

        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!sender.hasPermission("adminchat.use")) {
            MessageHelper.sendMessage(sender, config.getNoPermissionMsg());
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                MessageHelper.sendMessage(sender, config.getPlayersOnlyMsg());
                return true;
            }

            if (plugin.getChatManager().hasToggledChannel(player)) {
                String channel = plugin.getChatManager().getToggledChannel(player);
                MessageHelper.sendMessage(sender, config.getUsageToggledMsg()
                        .replace("{channel}", channel));
            } else {
                MessageHelper.sendMessage(sender, config.getUsageMsg());
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelp(sender);
                return true;

            case "reload":
                if (!sender.hasPermission("adminchat.admin")) {
                    MessageHelper.sendMessage(sender, config.getNoPermissionMsg());
                    return true;
                }
                plugin.reload();
                MessageHelper.sendMessage(sender, config.getReloadedMsg());
                return true;

            case "toggle":
                handleToggle(sender, args);
                return true;

            case "off":
                handleOff(sender);
                return true;
        }

        // Fall-through: treat args as a channel or message
        handleMessage(sender, args);
        return true;
    }

    private void handleToggle(CommandSender sender, String[] args) {
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!(sender instanceof Player player)) {
            MessageHelper.sendMessage(sender, config.getPlayersOnlyMsg());
            return;
        }

        if (args.length > 1 && sender.hasPermission("adminchat.admin")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                MessageHelper.sendMessage(sender, config.getPlayerNotFoundMsg());
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
            MessageHelper.sendMessage(sender, config.getPlayersOnlyMsg());
            return;
        }

        if (plugin.getChatManager().hasToggledChannel(player)) {
            String channel = plugin.getChatManager().getToggledChannel(player);
            plugin.getChatManager().clearToggledChannel(player);
            MessageHelper.sendMessage(sender, config.getChannelOffMsg()
                    .replace("{channel}", channel));
        } else {
            MessageHelper.sendMessage(sender, config.getNoToggledChannelMsg());
        }
    }

    private void handleMessage(CommandSender sender, String[] args) {
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!(sender instanceof Player player)) {
            MessageHelper.sendMessage(sender, config.getPlayersOnlyMsg());
            return;
        }

        String channel;
        String message;

        if (config.channelExists(args[0].toLowerCase())) {
            channel = args[0].toLowerCase();

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
        MessageHelper.sendMessage(sender, "&6&lAdminChat Help");
        MessageHelper.sendMessage(sender, "&e/achat <message> &7- Send to your default/toggled channel");
        MessageHelper.sendMessage(sender, "&e/achat <channel> <message> &7- Send to a specific channel");
        MessageHelper.sendMessage(sender, "&e/achat <channel> &7- Toggle a channel on");
        MessageHelper.sendMessage(sender, "&e/achat off &7- Exit your toggled channel");
        MessageHelper.sendMessage(sender, "&e/achat toggle [player] &7- Toggle visibility");

        if (sender.hasPermission("adminchat.admin")) {
            MessageHelper.sendMessage(sender, "&e/achat reload &7- Reload the configuration");
        }
        MessageHelper.sendMessage(sender, "&e/achat help &7- Show this help");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (args.length == 1) {
            String partial = args[0].toLowerCase();

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
                    if (player.getName().toLowerCase().startsWith(partial)) {
                        completions.add(player.getName());
                    }
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            String partial = args[1].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(partial)) {
                    completions.add(player.getName());
                }
            }
        }

        return completions;
    }
}