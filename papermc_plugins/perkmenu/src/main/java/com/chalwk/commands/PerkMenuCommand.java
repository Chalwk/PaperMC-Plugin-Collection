// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.commands;

import com.chalwk.PerkMenu;
import com.chalwk.config.PluginConfig;
import com.chalwk.managers.PerkManager;
import com.chalwk.util.MessageHelper;
import com.chalwk.util.Perk;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
 * Handles {@code /perks} and its subcommands.
 *
 * <p>The list views are chat-paginated. Each perk line is clickable and
 * hoverable: hovering shows the description, clicking opens the shop URL
 * for unowned perks or shows details for owned ones.</p>
 *
 * <p>The footer carries clickable Prev and Next buttons. They run the same
 * command the player would have typed, so filter state is preserved when
 * paging through owned or available views.</p>
 */
public class PerkMenuCommand implements TabExecutor {

    private final PerkMenu plugin;
    private final MessageHelper messageHelper;

    public PerkMenuCommand(PerkMenu plugin, MessageHelper messageHelper) {
        this.plugin = plugin;
        this.messageHelper = messageHelper;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {

        PluginConfig config = plugin.getConfigManager().getConfig();

        if (!sender.hasPermission("perkmenu.use")) {
            messageHelper.sendMessage(sender, config.getNoPermissionMsg());
            return true;
        }

        if (args.length == 0) {
            showList(sender, ListFilter.ALL, 1);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "help" -> {
                sendHelp(sender);
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("perkmenu.reload")) {
                    messageHelper.sendMessage(sender, config.getNoPermissionMsg());
                    return true;
                }
                plugin.reload();
                messageHelper.sendMessage(sender, config.getReloadedMsg());
                return true;
            }
            case "owned" -> {
                int page = parsePage(args, 1, config, sender);
                if (page < 0) return true;
                showList(sender, ListFilter.OWNED, page);
                return true;
            }
            case "available" -> {
                int page = parsePage(args, 1, config, sender);
                if (page < 0) return true;
                showList(sender, ListFilter.AVAILABLE, page);
                return true;
            }
            case "info" -> {
                if (args.length < 2) {
                    messageHelper.sendMessage(sender, config.getUnknownCommandMsg());
                    return true;
                }
                showInfo(sender, args[1]);
                return true;
            }
            case "check" -> {
                if (!sender.hasPermission("perkmenu.check")) {
                    messageHelper.sendMessage(sender, config.getNoPermissionMsg());
                    return true;
                }
                if (args.length < 2) {
                    messageHelper.sendMessage(sender, config.getUnknownCommandMsg());
                    return true;
                }
                showCheck(sender, args[1]);
                return true;
            }
        }

        try {
            int page = Integer.parseInt(sub);
            showList(sender, ListFilter.ALL, page);
            return true;
        } catch (NumberFormatException ignored) {
            // fall through to unknown command
        }

        messageHelper.sendMessage(sender, config.getUnknownCommandMsg());
        return true;
    }

    private int parsePage(String[] args, int index, PluginConfig config, CommandSender sender) {
        if (args.length <= index) return 1;
        try {
            return Integer.parseInt(args[index]);
        } catch (NumberFormatException e) {
            messageHelper.sendMessage(sender, config.getInvalidPageMsg());
            return -1;
        }
    }

    private void showList(CommandSender sender, ListFilter filter, int page) {
        PluginConfig config = plugin.getConfigManager().getConfig();
        PerkManager manager = plugin.getPerkManager();

        if (!(sender instanceof Player player)) {
            messageHelper.sendMessage(sender, config.getPlayersOnlyMsg());
            return;
        }

        List<Perk> source = switch (filter) {
            case OWNED -> manager.getOwnedBy(player);
            case AVAILABLE -> manager.getUnownedBy(player);
            case ALL -> config.isShowOwnedInList() ? manager.getAll() : manager.getUnownedBy(player);
        };

        if (source.isEmpty()) {
            messageHelper.sendMessage(sender, config.getNoPerksMsg());
            return;
        }

        int perPage = Math.max(1, config.getPerksPerPage());
        int totalPages = (int) Math.ceil((double) source.size() / perPage);
        page = Math.max(1, Math.min(page, totalPages));

        int start = (page - 1) * perPage;
        int end = Math.min(start + perPage, source.size());
        List<Perk> pageItems = source.subList(start, end);

        String filterLabel = switch (filter) {
            case OWNED -> config.getFilterOwnedMsg();
            case AVAILABLE -> config.getFilterAvailableMsg();
            case ALL -> config.getFilterAllMsg();
        };

        // Header block: title line, summary line, then a blank line.
        messageHelper.sendMessage(sender, config.getListHeaderMsg()
                .replace("{filter}", filterLabel)
                .replace("{page}", String.valueOf(page))
                .replace("{total_pages}", String.valueOf(totalPages)));

        int ownedCount = manager.getOwnedBy(player).size();
        int totalCount = manager.getAll().size();
        double spent = manager.getTotalListedValue(player);
        messageHelper.sendMessage(sender, config.getSummaryMsg()
                .replace("{owned}", String.valueOf(ownedCount))
                .replace("{total}", String.valueOf(totalCount))
                .replace("{spent}", formatPrice(spent)));

        messageHelper.sendComponent(player, Component.empty());

        // Perk lines.
        for (Perk perk : pageItems) {
            boolean owned = manager.isOwnedBy(perk, player);
            messageHelper.sendComponent(player, buildPerkLine(perk, owned));
        }

        // Blank line before the footer.
        messageHelper.sendComponent(player, Component.empty());

        messageHelper.sendComponent(player, buildFooter(page, totalPages, filter));
    }

    /**
     * Builds the pagination footer as a single Component. Prev and Next are
     * clickable buttons that run the equivalent command for the current
     * filter, so paging through {@code /perks owned} stays in that view.
     */
    private Component buildFooter(int page, int totalPages, ListFilter filter) {
        PluginConfig config = plugin.getConfigManager().getConfig();
        LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();

        List<Component> parts = new ArrayList<>();
        boolean hasPrev = page > 1;
        boolean hasNext = page < totalPages;

        if (hasPrev) {
            Component prev = legacy.deserialize(config.getFooterPrevButtonMsg())
                    .clickEvent(ClickEvent.runCommand(buildPageCommand(filter, page - 1)))
                    .hoverEvent(HoverEvent.showText(Component.text("Go to page " + (page - 1))));
            parts.add(prev);
        }

        String pageInfo = config.getFooterPageInfoMsg()
                .replace("{page}", String.valueOf(page))
                .replace("{total_pages}", String.valueOf(totalPages));
        parts.add(legacy.deserialize(pageInfo));

        if (hasNext) {
            Component next = legacy.deserialize(config.getFooterNextButtonMsg())
                    .clickEvent(ClickEvent.runCommand(buildPageCommand(filter, page + 1)))
                    .hoverEvent(HoverEvent.showText(Component.text("Go to page " + (page + 1))));
            parts.add(next);
        }

        String hint = config.getFooterHintMsg();
        if (!hint.isEmpty()) {
            parts.add(legacy.deserialize(hint));
        }

        Component footer = Component.empty();
        boolean first = true;
        for (Component part : parts) {
            if (!first) footer = footer.append(Component.text(" "));
            footer = footer.append(part);
            first = false;
        }
        return footer;
    }

    private String buildPageCommand(ListFilter filter, int page) {
        return switch (filter) {
            case OWNED -> "/perks owned " + page;
            case AVAILABLE -> "/perks available " + page;
            case ALL -> "/perks " + page;
        };
    }

    private Component buildPerkLine(Perk perk, boolean owned) {
        PluginConfig config = plugin.getConfigManager().getConfig();

        String template = owned ? config.getPerkOwnedLineMsg() : config.getPerkUnownedLineMsg();
        String text = template
                .replace("{name}", perk.name())
                .replace("{price}", formatPrice(perk.price()))
                .replace("{category}", perk.category());

        Component line = LegacyComponentSerializer.legacyAmpersand().deserialize(text);

        ClickEvent click = owned
                ? ClickEvent.runCommand("/perks info " + perk.id())
                : ClickEvent.openUrl(plugin.getPerkManager().buildBuyUrl(perk));

        Component hoverText = LegacyComponentSerializer.legacyAmpersand().deserialize(
                String.join("\n", perk.description()));

        return line.clickEvent(click).hoverEvent(HoverEvent.showText(hoverText));
    }

    private void showInfo(CommandSender sender, String query) {
        PluginConfig config = plugin.getConfigManager().getConfig();
        PerkManager manager = plugin.getPerkManager();

        Perk perk = manager.find(query);
        if (perk == null) {
            messageHelper.sendMessage(sender, config.getUnknownPerkMsg().replace("{perk}", query));
            return;
        }

        messageHelper.sendMessage(sender, config.getInfoHeaderMsg().replace("{name}", perk.name()));
        messageHelper.sendMessage(sender, config.getInfoCategoryMsg().replace("{category}", perk.category()));
        messageHelper.sendMessage(sender, config.getInfoPriceMsg().replace("{price}", formatPrice(perk.price())));

        boolean owned = (sender instanceof Player player) && manager.isOwnedBy(perk, player);
        messageHelper.sendMessage(sender, owned ? config.getInfoStatusOwnedMsg() : config.getInfoStatusUnownedMsg());

        messageHelper.sendMessage(sender, config.getInfoDescriptionHeaderMsg());
        for (String line : perk.description()) {
            messageHelper.sendMessage(sender, config.getInfoDescriptionLineMsg().replace("{line}", line));
        }

        messageHelper.sendMessage(sender, owned ? config.getInfoOwnedHintMsg() : config.getInfoBuyHintMsg());
    }

    private void showCheck(CommandSender sender, String playerName) {
        PluginConfig config = plugin.getConfigManager().getConfig();
        PerkManager manager = plugin.getPerkManager();

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            messageHelper.sendMessage(sender, config.getUnknownPerkMsg().replace("{perk}", playerName));
            return;
        }

        int owned = manager.getOwnedBy(target).size();
        int total = manager.getAll().size();
        double value = manager.getTotalListedValue(target);

        messageHelper.sendMessage(sender, config.getCheckHeaderMsg().replace("{player}", target.getName()));
        messageHelper.sendMessage(sender, config.getCheckSummaryMsg()
                .replace("{owned}", String.valueOf(owned))
                .replace("{total}", String.valueOf(total))
                .replace("{spent}", formatPrice(value)));
    }

    private void sendHelp(CommandSender sender) {
        for (String line : plugin.getConfigManager().getConfig().getHelpMsg()) {
            messageHelper.sendMessage(sender, line);
        }
    }

    private static String formatPrice(double price) {
        return String.format("$%.2f", price);
    }

    private enum ListFilter {
        ALL, OWNED, AVAILABLE
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
            options.add("owned");
            options.add("available");
            options.add("info");
            if (sender.hasPermission("perkmenu.check")) options.add("check");
            if (sender.hasPermission("perkmenu.reload")) options.add("reload");

            for (String opt : options) {
                if (opt.startsWith(partial)) completions.add(opt);
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            String partial = args[1].toLowerCase(Locale.ROOT);

            if (sub.equals("info")) {
                for (Perk perk : plugin.getPerkManager().getAll()) {
                    if (perk.id().toLowerCase(Locale.ROOT).startsWith(partial)) {
                        completions.add(perk.id());
                    }
                }
            } else if (sub.equals("check") && sender.hasPermission("perkmenu.check")) {
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