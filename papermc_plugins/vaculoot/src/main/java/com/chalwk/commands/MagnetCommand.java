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

public class MagnetCommand implements TabExecutor {
    private final VacuLoot plugin;

    public MagnetCommand(VacuLoot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {

        if (!sender.hasPermission("magnet.use")) {
            MessageHelper.sendMessage(sender, "&cYou don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                MessageHelper.sendMessage(sender, "&cOnly players can use this command!");
                return true;
            }

            toggleOwnMagnet(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

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
                    MessageHelper.sendMessage(sender, "&cUnknown command. Use /magnet help");
                }
                break;
        }

        return true;
    }

    private void toggleOwnMagnet(Player player) {
        MagnetManager magnetManager = plugin.getMagnetManager();

        if (magnetManager.hasCooldown(player)) {
            int remaining = magnetManager.getRemainingCooldown(player);
            MessageHelper.sendMessage(player,
                    plugin.getConfigManager().getConfig().getCooldownMsg()
                            .replace("{seconds}", String.valueOf(remaining)));
            return;
        }

        MagnetManager.ToggleResult result = magnetManager.toggleMagnet(player);
        PluginConfig config = plugin.getConfigManager().getConfig();

        switch (result) {
            case INSUFFICIENT_FUNDS:
                double cost = config.getToggleCost();
                MessageHelper.sendMessage(player,
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
                    MessageHelper.sendMessage(player, message);

                    if (config.isEconomyEnabled() && config.getToggleCost() > 0) {
                        MessageHelper.sendMessage(player,
                                "&7Cost: &e$" + String.format("%.2f", config.getToggleCost()));
                    }
                } else {
                    MessageHelper.sendMessage(player, config.getDisabledMsg());
                }
                break;

            case COOLDOWN:
                int remaining = magnetManager.getRemainingCooldown(player);
                MessageHelper.sendMessage(player,
                        config.getCooldownMsg()
                                .replace("{seconds}", String.valueOf(remaining)));
                break;
        }
    }

    private void handleToggle(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                MessageHelper.sendMessage(sender, "&cOnly players can use this command!");
                return;
            }
            toggleOwnMagnet(player);
        } else if (args.length == 2) {
            if (!sender.hasPermission("magnet.use.others")) {
                MessageHelper.sendMessage(sender, "&cYou don't have permission to toggle magnet for others!");
                return;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                MessageHelper.sendMessage(sender,
                        plugin.getConfigManager().getConfig().getPlayerNotFoundMsg());
                return;
            }

            boolean newState = plugin.getMagnetManager().isMagnetActive(target);
            plugin.getMagnetManager().setMagnetState(target, !newState);

            String stateMsg = !newState ? "enabled" : "disabled";
            MessageHelper.sendMessage(sender,
                    plugin.getConfigManager().getConfig().getToggledForMsg()
                            .replace("{player}", target.getName())
                            .replace("{state}", stateMsg));
            MessageHelper.sendMessage(target,
                    plugin.getConfigManager().getConfig().getToggledByMsg()
                            .replace("{state}", stateMsg)
                            .replace("{sender}", sender.getName()));
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("magnet.admin")) {
            MessageHelper.sendMessage(sender, "&cYou don't have permission to reload the configuration!");
            return;
        }

        plugin.reload();
        MessageHelper.sendMessage(sender, plugin.getConfigManager().getConfig().getReloadedMsg());
    }

    private void handleCheck(CommandSender sender, String[] args) {
        Player target;

        if (args.length > 1 && sender.hasPermission("magnet.use.others")) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                MessageHelper.sendMessage(sender,
                        plugin.getConfigManager().getConfig().getPlayerNotFoundMsg());
                return;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            MessageHelper.sendMessage(sender, "&cYou must specify a player!");
            return;
        }

        boolean isActive = plugin.getMagnetManager().isMagnetActive(target);
        String tier = plugin.getMagnetManager().getPlayerTier(target);
        double range = plugin.getConfigManager().getConfig().getMagnetRange(tier);

        String status = isActive ? "&aACTIVE" : "&cINACTIVE";
        String message = plugin.getConfigManager().getConfig().getStatusMsg()
                .replace("{player}", target.getName())
                .replace("{status}", status)
                .replace("{tier}", tier)
                .replace("{range}", String.valueOf(range));

        MessageHelper.sendMessage(sender, message);
    }

    private void handleTier(CommandSender sender, String[] args) {
        if (!sender.hasPermission("magnet.admin")) {
            MessageHelper.sendMessage(sender, "&cYou don't have permission to change tiers!");
            return;
        }

        if (args.length < 3) {
            MessageHelper.sendMessage(sender, "&cUsage: /magnet tier <player> <tier>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            MessageHelper.sendMessage(sender,
                    plugin.getConfigManager().getConfig().getPlayerNotFoundMsg());
            return;
        }

        String tier = args[2].toLowerCase();
        if (!plugin.getConfigManager().getConfig().getMagnetTiers().containsKey(tier)) {
            MessageHelper.sendMessage(sender,
                    plugin.getConfigManager().getConfig().getInvalidTierMsg());
            return;
        }

        plugin.getMagnetManager().setPlayerTier(target, tier);
        MessageHelper.sendMessage(sender,
                plugin.getConfigManager().getConfig().getTierSetMsg()
                        .replace("{player}", target.getName())
                        .replace("{tier}", tier));
        MessageHelper.sendMessage(target,
                plugin.getConfigManager().getConfig().getTierChangedMsg()
                        .replace("{tier}", tier));
    }

    private void handleToggleOther(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            MessageHelper.sendMessage(sender,
                    plugin.getConfigManager().getConfig().getPlayerNotFoundMsg());
            return;
        }

        boolean newState = !plugin.getMagnetManager().isMagnetActive(target);
        plugin.getMagnetManager().setMagnetState(target, newState);

        String stateMsg = newState ? "enabled" : "disabled";
        MessageHelper.sendMessage(sender,
                plugin.getConfigManager().getConfig().getToggledForMsg()
                        .replace("{player}", target.getName())
                        .replace("{state}", stateMsg));
        MessageHelper.sendMessage(target,
                plugin.getConfigManager().getConfig().getToggledByMsg()
                        .replace("{state}", stateMsg)
                        .replace("{sender}", sender.getName()));
    }

    private void sendHelp(CommandSender sender) {
        PluginConfig config = plugin.getConfigManager().getConfig();

        MessageHelper.sendMessage(sender, "&6&lVacuLoot Help");
        MessageHelper.sendMessage(sender, "&e/magnet &7- Toggle your magnet");
        MessageHelper.sendMessage(sender, "&e/magnet toggle [player] &7- Toggle magnet for yourself or another player");
        MessageHelper.sendMessage(sender, "&e/magnet check [player] &7- Check magnet status");

        if (sender.hasPermission("magnet.admin")) {
            MessageHelper.sendMessage(sender, "&e/magnet tier <player> <tier> &7- Set player's magnet tier");
            MessageHelper.sendMessage(sender, "&e/magnet reload &7- Reload configuration");
        }

        MessageHelper.sendMessage(sender, "&e/magnet help &7- Show this help");

        if (config.isEconomyEnabled() && config.getToggleCost() > 0) {
            MessageHelper.sendMessage(sender, "&7Cost per toggle: &e$" + String.format("%.2f", config.getToggleCost()));
        }
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
                    if (player.getName().toLowerCase().startsWith(partial)) {
                        completions.add(player.getName());
                    }
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("toggle") || subCommand.equals("check")
                    || (subCommand.equals("tier") && sender.hasPermission("magnet.admin"))) {
                String partial = args[1].toLowerCase();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(partial)) {
                        completions.add(player.getName());
                    }
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("tier")) {
            String partial = args[2].toLowerCase();
            for (String tier : plugin.getConfigManager().getConfig().getMagnetTiers().keySet()) {
                if (tier.startsWith(partial)) {
                    completions.add(tier);
                }
            }
        }

        return completions;
    }
}