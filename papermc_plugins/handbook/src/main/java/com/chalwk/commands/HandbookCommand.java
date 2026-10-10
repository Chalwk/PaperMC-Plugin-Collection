// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.commands;

import com.chalwk.Handbook;
import com.chalwk.config.PluginConfig;
import com.chalwk.util.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Handles {@code /handbook} and its subcommands.
 */
public class HandbookCommand implements TabExecutor {

    private final Handbook plugin;
    private final MessageHelper messageHelper;

    public HandbookCommand(Handbook plugin, MessageHelper messageHelper) {
        this.plugin = plugin;
        this.messageHelper = messageHelper;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {

        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!sender.hasPermission("handbook.use")) {
            messageHelper.sendMessage(sender, config.getNoPermissionMsg());
            return true;
        }

        if (args.length == 0) {
            giveToSelf(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "help" -> {
                sendHelp(sender);
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("handbook.reload")) {
                    messageHelper.sendMessage(sender, config.getNoPermissionMsg());
                    return true;
                }
                plugin.reload();
                messageHelper.sendMessage(sender, config.getReloadedMsg());
                return true;
            }
            case "give" -> {
                if (!sender.hasPermission("handbook.give")) {
                    messageHelper.sendMessage(sender, config.getNoPermissionMsg());
                    return true;
                }
                if (args.length < 2) {
                    messageHelper.sendMessage(sender, config.getUnknownCommandMsg());
                    return true;
                }
                giveToOther(sender, args[1]);
                return true;
            }
        }

        messageHelper.sendMessage(sender, config.getUnknownCommandMsg());
        return true;
    }

    private void giveToSelf(CommandSender sender) {
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!(sender instanceof Player player)) {
            messageHelper.sendMessage(sender, config.getPlayersOnlyMsg());
            return;
        }

        boolean given = plugin.getHandbookManager().giveToPlayer(player);
        if (!given) {
            messageHelper.sendMessage(sender, config.getInventoryFullMsg());
            return;
        }

        plugin.getHandbookManager().markReceived(player);
        messageHelper.sendMessage(sender, config.getReceivedMsg());
    }

    private void giveToOther(CommandSender sender, String targetName) {
        PluginConfig config = plugin.getConfigManager().getConfig();

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            messageHelper.sendMessage(sender, config.getPlayerNotFoundMsg());
            return;
        }

        boolean given = plugin.getHandbookManager().giveToPlayer(target);
        if (!given) {
            messageHelper.sendMessage(sender, config.getInventoryFullMsg());
            return;
        }

        plugin.getHandbookManager().markReceived(target);
        messageHelper.sendMessage(sender, config.getGivenMsg().replace("{player}", target.getName()));
    }

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
            if (sender.hasPermission("handbook.give")) options.add("give");
            if (sender.hasPermission("handbook.reload")) options.add("reload");

            for (String opt : options) {
                if (opt.startsWith(partial)) completions.add(opt);
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")
                && sender.hasPermission("handbook.give")) {
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