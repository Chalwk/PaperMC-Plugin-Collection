// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.commands;

import com.chalwk.VacuLoot;
import com.chalwk.config.PluginConfig;
import com.chalwk.managers.MagnetManager;
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

public class MagnetCommand implements TabExecutor {
    private final VacuLoot plugin;
    private final MessageHelper messageHelper;

    public MagnetCommand(VacuLoot plugin, MessageHelper messageHelper) {
        this.plugin = plugin;
        this.messageHelper = messageHelper;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {

        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!sender.hasPermission("magnet.use")) {
            messageHelper.sendMessage(sender, config.getNoPermissionMsg());
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                messageHelper.sendMessage(sender, config.getPlayersOnlyMsg());
                return true;
            }
            toggleOwnMagnet(player);
            return true;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);

        switch (subCommand) {
            case "toggle":
                handleToggle(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            case "check":
                handleCheck(sender, args);
                break;
            case "tier":
                handleTier(sender, args);
                break;
            case "help":
                sendHelp(sender);
                break;
            default:
                if (sender.hasPermission("magnet.use.others")) {
                    handleToggleOther(sender, args[0]);
                } else {
                    messageHelper.sendMessage(sender, config.getUnknownCommandMsg());
                }
                break;
        }

        return true;
    }

    private void toggleOwnMagnet(Player player) {
        MagnetManager magnetManager = plugin.getMagnetManager();
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (magnetManager.hasCooldown(player)) {
            int remaining = magnetManager.getRemainingCooldown(player);
            messageHelper.sendMessage(player,
                    config.getCooldownMsg().replace("{seconds}", String.valueOf(remaining)));
            return;
        }

        MagnetManager.ToggleResult result = magnetManager.toggleMagnet(player);

        switch (result) {
            case INSUFFICIENT_FUNDS:
                double cost = config.getToggleCost();
                messageHelper.sendMessage(player,
                        config.getInsufficientFundsMsg()
                                .replace("{amount}", String.format("%.2f", cost)));
                break;

            case SUCCESS:
                boolean newState = magnetManager.isMagnetActive(player);
                String tier = magnetManager.getPlayerTier(player);
                double range = config.getMagnetRange(tier);

                if (newState) {
                    String message = config.getEnabledMsg()
                            .replace("{tier}", tier)
                            .replace("{range}", String.valueOf(range));
                    messageHelper.sendMessage(player, message);

                    if (config.isEconomyEnabled() && config.getToggleCost() > 0) {
                        messageHelper.sendMessage(player,
                                config.getCostLineMsg()
                                        .replace("{amount}", String.format("%.2f", config.getToggleCost())));
                    }
                } else {
                    messageHelper.sendMessage(player, config.getDisabledMsg());
                }
                break;

            case COOLDOWN:
                int remaining = magnetManager.getRemainingCooldown(player);
                messageHelper.sendMessage(player,
                        config.getCooldownMsg().replace("{seconds}", String.valueOf(remaining)));
                break;
        }
    }

    private void handleToggle(CommandSender sender, String[] args) {
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                messageHelper.sendMessage(sender, config.getPlayersOnlyMsg());
                return;
            }
            toggleOwnMagnet(player);
        } else if (args.length == 2) {
            if (!sender.hasPermission("magnet.use.others")) {
                messageHelper.sendMessage(sender, config.getOthersNoPermissionMsg());
                return;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                messageHelper.sendMessage(sender, config.getPlayerNotFoundMsg());
                return;
            }

            boolean newState = !plugin.getMagnetManager().isMagnetActive(target);
            plugin.getMagnetManager().setMagnetState(target, newState);

            String stateMsg = newState ? "enabled" : "disabled";
            messageHelper.sendMessage(sender,
                    config.getToggledForMsg()
                            .replace("{player}", target.getName())
                            .replace("{state}", stateMsg));
            messageHelper.sendMessage(target,
                    config.getToggledByMsg()
                            .replace("{state}", stateMsg)
                            .replace("{sender}", sender.getName()));
        }
    }

    private void handleReload(CommandSender sender) {
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!sender.hasPermission("magnet.admin")) {
            messageHelper.sendMessage(sender, config.getAdminNoPermissionMsg());
            return;
        }

        plugin.reload();
        messageHelper.sendMessage(sender, config.getReloadedMsg());
    }

    private void handleCheck(CommandSender sender, String[] args) {
        PluginConfig config = plugin.getConfigManager().getConfig();
        Player target;

        if (args.length > 1 && sender.hasPermission("magnet.use.others")) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                messageHelper.sendMessage(sender, config.getPlayerNotFoundMsg());
                return;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            messageHelper.sendMessage(sender, config.getSpecifyPlayerMsg());
            return;
        }

        boolean isActive = plugin.getMagnetManager().isMagnetActive(target);
        String tier = plugin.getMagnetManager().getPlayerTier(target);
        double range = config.getMagnetRange(tier);

        String status = isActive ? "&aACTIVE" : "&cINACTIVE";
        String message = config.getStatusMsg()
                .replace("{player}", target.getName())
                .replace("{status}", status)
                .replace("{tier}", tier)
                .replace("{range}", String.valueOf(range));

        messageHelper.sendMessage(sender, message);
    }

    private void handleTier(CommandSender sender, String[] args) {
        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!sender.hasPermission("magnet.admin")) {
            messageHelper.sendMessage(sender, config.getAdminNoPermissionMsg());
            return;
        }

        if (args.length < 3) {
            messageHelper.sendMessage(sender, config.getTierUsageMsg());
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            messageHelper.sendMessage(sender, config.getPlayerNotFoundMsg());
            return;
        }

        String tier = args[2].toLowerCase(Locale.ROOT);
        if (!config.getMagnetTiers().containsKey(tier)) {
            messageHelper.sendMessage(sender, config.getInvalidTierMsg());
            return;
        }

        plugin.getMagnetManager().setPlayerTier(target, tier);
        messageHelper.sendMessage(sender,
                config.getTierSetMsg()
                        .replace("{player}", target.getName())
                        .replace("{tier}", tier));
        messageHelper.sendMessage(target,
                config.getTierChangedMsg()
                        .replace("{tier}", tier));
    }

    private void handleToggleOther(CommandSender sender, String playerName) {
        PluginConfig config = plugin.getConfigManager().getConfig();

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            messageHelper.sendMessage(sender, config.getPlayerNotFoundMsg());
            return;
        }

        boolean newState = !plugin.getMagnetManager().isMagnetActive(target);
        plugin.getMagnetManager().setMagnetState(target, newState);

        String stateMsg = newState ? "enabled" : "disabled";
        messageHelper.sendMessage(sender,
                config.getToggledForMsg()
                        .replace("{player}", target.getName())
                        .replace("{state}", stateMsg));
        messageHelper.sendMessage(target,
                config.getToggledByMsg()
                        .replace("{state}", stateMsg)
                        .replace("{sender}", sender.getName()));
    }

    private void sendHelp(CommandSender sender) {
        PluginConfig config = plugin.getConfigManager().getConfig();

        for (String line : config.getHelpLines()) {
            messageHelper.sendMessage(sender, line);
        }

        if (config.isEconomyEnabled() && config.getToggleCost() > 0) {
            messageHelper.sendMessage(sender,
                    config.getHelpCostMsg()
                            .replace("{amount}", String.format("%.2f", config.getToggleCost())));
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
            options.add("toggle");
            options.add("check");
            options.add("help");
            if (sender.hasPermission("magnet.admin")) {
                options.add("reload");
                options.add("tier");
            }

            for (String opt : options) {
                if (opt.startsWith(partial)) {
                    completions.add(opt);
                }
            }

            if (sender.hasPermission("magnet.use.others")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase(Locale.ROOT).startsWith(partial)) {
                        completions.add(player.getName());
                    }
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase(Locale.ROOT);

            if (subCommand.equals("toggle") || subCommand.equals("check")
                    || (subCommand.equals("tier") && sender.hasPermission("magnet.admin"))) {
                String partial = args[1].toLowerCase(Locale.ROOT);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase(Locale.ROOT).startsWith(partial)) {
                        completions.add(player.getName());
                    }
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("tier")) {
            String partial = args[2].toLowerCase(Locale.ROOT);
            for (String tier : plugin.getConfigManager().getConfig().getMagnetTiers().keySet()) {
                if (tier.startsWith(partial)) {
                    completions.add(tier);
                }
            }
        }

        return completions;
    }
}