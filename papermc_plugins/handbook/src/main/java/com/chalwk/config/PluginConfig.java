// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * Parsed settings and messages from config.yml. Book content lives in
 * {@link HandbookConfig}.
 */
public class PluginConfig {

    private boolean giveOnFirstJoin = true;
    private boolean onlyFirstJoin = true;
    private boolean dropWhenInventoryFull = true;

    // Minecraft renders at most 14 lines per page. We count raw content
    // lines here, before wrapping. Default 9 leaves room for our header
    // and a few wrapped lines.
    private int linesPerPage = 9;

    // Minecraft's hard cap for written books. Enforced internally; we
    // mirror the number so we can clamp before handing pages to the meta.
    private int maxPages = 100;

    private String noPermissionMsg = "&cYou don't have permission to use this command!";
    private String playersOnlyMsg = "&cOnly players can use this command!";
    private String unknownCommandMsg = "&cUnknown command. Use /handbook help";
    private String reloadedMsg = "&aHandbook configuration reloaded!";
    private String receivedMsg = "&aYou have received the Compendium of the Realm.";
    private String givenMsg = "&aGave the handbook to &f{player}&a.";
    private String playerNotFoundMsg = "&cPlayer not found.";
    private String inventoryFullMsg = "&eYour inventory is full. The handbook has been dropped at your feet.";
    private List<String> helpMsg = List.of(
            "&6&lHandbook Help",
            "&e/handbook &7- Get a copy of the server handbook",
            "&e/handbook give <player> &7- Give the handbook to another player",
            "&e/handbook reload &7- Reload the configuration",
            "&e/handbook help &7- Show this help"
    );

    public void loadFromConfig(ConfigurationSection config) {
        giveOnFirstJoin = config.getBoolean("give_on_first_join", giveOnFirstJoin);
        onlyFirstJoin = config.getBoolean("only_first_join", onlyFirstJoin);
        dropWhenInventoryFull = config.getBoolean("drop_when_inventory_full", dropWhenInventoryFull);

        linesPerPage = config.getInt("lines_per_page", linesPerPage);
        maxPages = config.getInt("max_pages", maxPages);

        ConfigurationSection messages = config.getConfigurationSection("messages");
        if (messages == null) return;

        noPermissionMsg = messages.getString("no_permission", noPermissionMsg);
        playersOnlyMsg = messages.getString("players_only", playersOnlyMsg);
        unknownCommandMsg = messages.getString("unknown_command", unknownCommandMsg);
        reloadedMsg = messages.getString("reloaded", reloadedMsg);
        receivedMsg = messages.getString("received", receivedMsg);
        givenMsg = messages.getString("given", givenMsg);
        playerNotFoundMsg = messages.getString("player_not_found", playerNotFoundMsg);
        inventoryFullMsg = messages.getString("inventory_full", inventoryFullMsg);

        List<String> help = messages.getStringList("help");
        if (!help.isEmpty()) {
            helpMsg = List.copyOf(help);
        }
    }

    public boolean isGiveOnFirstJoin() { return giveOnFirstJoin; }
    public boolean isOnlyFirstJoin() { return onlyFirstJoin; }
    public boolean isDropWhenInventoryFull() { return dropWhenInventoryFull; }

    public int getLinesPerPage() { return linesPerPage; }
    public int getMaxPages() { return maxPages; }

    public String getNoPermissionMsg() { return noPermissionMsg; }
    public String getPlayersOnlyMsg() { return playersOnlyMsg; }
    public String getUnknownCommandMsg() { return unknownCommandMsg; }
    public String getReloadedMsg() { return reloadedMsg; }
    public String getReceivedMsg() { return receivedMsg; }
    public String getGivenMsg() { return givenMsg; }
    public String getPlayerNotFoundMsg() { return playerNotFoundMsg; }
    public String getInventoryFullMsg() { return inventoryFullMsg; }
    public List<String> getHelpMsg() { return helpMsg; }
}