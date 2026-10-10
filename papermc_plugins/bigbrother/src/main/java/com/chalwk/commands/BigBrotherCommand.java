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
import java.util.Locale;

/**
 * Handles {@code /bigbrother} and its subcommands.
 *
 * <p>Subcommand dispatch is a plain switch. Spy subcommands are resolved
 * through {@link SpyType#fromCommand(String)} so the enum stays the single
 * source of truth for command names, config keys, and permission nodes.</p>
 */
public class BigBrotherCommand implements TabExecutor {

    private final BigBrother plugin;
    private final MessageHelper messageHelper;

    public BigBrotherCommand(BigBrother plugin, MessageHelper messageHelper) {
        this.plugin = plugin;
        this.messageHelper = messageHelper;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {

        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!sender.hasPermission("bigbrother.use")) {
            messageHelper.sendMessage(sender, config.getNoPermissionMsg());
            return true;
        }

        // Bare /bigbrother toggles the global switch. Console has no UUID-based
        // state to toggle, so it's player-only.
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                messageHelper.sendMessage(sender, config.getPlayersOnlyMsg());
                return true;
            }

            boolean newState = plugin.getSpyManager().toggleGlobal(player);
            String message = newState ? config.getGlobalEnabledMsg() : config.getGlobalDisabledMsg();
            messageHelper.sendMessage(sender, message);
            return true;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);

        switch (subCommand) {
            case "help":
                sendHelp(sender);
                return true;

            case "reload":
                // Reload is gated behind a second permission on top of the base
                // bigbrother.use. Not everyone who can toggle spies can reload.
                if (!sender.hasPermission("bigbrother.reload")) {
                    messageHelper.sendMessage(sender, config.getNoPermissionMsg());
                    return true;
                }
                plugin.reload();
                messageHelper.sendMessage(sender, config.getReloadedMsg());
                return true;

            case "status":
                if (!(sender instanceof Player player)) {
                    messageHelper.sendMessage(sender, config.getPlayersOnlyMsg());
                    return true;
                }
                messageHelper.sendMessage(sender, plugin.getSpyManager().getStatusMessage(player));
                return true;

            default:
                SpyType spyType = SpyType.fromCommand(subCommand);
                if (spyType != null) {
                    handleSpyToggle(sender, args, spyType);
                    return true;
                }

                messageHelper.sendMessage(sender, config.getUnknownCommandMsg());
                return true;
        }
    }

    /**
     * Handles toggling a single spy type, for either the sender or another player.
     *
     * <p>Command form is {@code /bigbrother <spy> [player]}. The optional player
     * argument requires the matching {@code .toggle.others} permission.</p>
     */
    private void handleSpyToggle(CommandSender sender, String[] args, SpyType spyType) {
        if (!(sender instanceof Player player)) {
            messageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getPlayersOnlyMsg());
            return;
        }

        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!player.hasPermission(spyType.getPermission())) {
            messageHelper.sendMessage(sender, config.getNoPermissionMsg());
            return;
        }

        if (args.length > 1) {
            if (!player.hasPermission(spyType.getPermissionOthers())) {
                messageHelper.sendMessage(sender, config.getNoPermissionMsg());
                return;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                messageHelper.sendMessage(sender, config.getPlayerNotFoundMsg());
                return;
            }

            boolean enabled = plugin.getSpyManager().toggleSpyForPlayer(target, spyType);
            String state = enabled ? config.getStateEnabled() : config.getStateDisabled();
            messageHelper.sendMessage(sender,
                    config.getSpyToggledOtherMsg()
                            .replace("{spy}", spyType.getConfigKey())
                            .replace("{state}", state)
                            .replace("{player}", target.getName()));
        } else {
            boolean enabled = plugin.getSpyManager().toggleSpy(player, spyType);
            String state = enabled ? config.getStateEnabled() : config.getStateDisabled();
            messageHelper.sendMessage(sender,
                    config.getSpyToggledSelfMsg()
                            .replace("{spy}", spyType.getConfigKey())
                            .replace("{state}", state));
        }
    }

    /**
     * Prints the help block. The spy entries are generated from
     * {@link SpyType#values()} so adding a new spy type to the enum is enough
     * to have it show up here.
     */
    private void sendHelp(CommandSender sender) {
        PluginConfig config = plugin.getConfigManager().getConfig();
        messageHelper.sendMessage(sender, config.getHelpHeaderMsg());
        messageHelper.sendMessage(sender, config.getHelpToggleAllMsg());
        messageHelper.sendMessage(sender, config.getHelpStatusMsg());

        for (SpyType type : SpyType.values()) {
            messageHelper.sendMessage(sender,
                    config.getHelpSpyEntryMsg()
                            .replace("{command}", type.getCommand())
                            .replace("{spy}", type.getConfigKey()));
        }

        messageHelper.sendMessage(sender, config.getHelpReloadMsg());
        messageHelper.sendMessage(sender, config.getHelpHelpMsg());
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
            if (sender.hasPermission("bigbrother.reload")) {
                options.add("reload");
            }
            // Only suggest spy subcommands the sender can actually use.
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
            // Second argument is a player name, and only for spy subcommands
            // where the sender has the .toggle.others permission.
            SpyType spyType = SpyType.fromCommand(args[0]);
            if (spyType != null && sender.hasPermission(spyType.getPermissionOthers())) {
                String partial = args[1].toLowerCase(Locale.ROOT);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase(Locale.ROOT).startsWith(partial)) {
                        completions.add(player.getName());
                    }
                }
            }
        }

        return completions;
    }
}