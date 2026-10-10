// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * Parsed message strings and general settings. Perks themselves live in
 * {@link com.chalwk.managers.PerkManager}.
 */
public class PluginConfig {

    private int perksPerPage = 6;
    private String buyUrlTemplate = "https://jericraft-shop.tebex.io/package/{package_id}";
    private boolean showOwnedInList = true;

    private String noPermissionMsg = "&cYou don't have permission to use this command!";
    private String playersOnlyMsg = "&cOnly players can use this command!";
    private String unknownCommandMsg = "&cUnknown command. Use /perks help";
    private String reloadedMsg = "&aConfiguration reloaded!";
    private String noPerksMsg = "&7No perks to display.";
    private String unknownPerkMsg = "&cUnknown perk: &f{perk}";
    private String invalidPageMsg = "&cInvalid page number.";
    private String listHeaderMsg = "&6&lPerks &7- &f{filter} &7(Page &e{page}&7/&e{total_pages}&7)";
    private String summaryMsg = "&7You own &a{owned}&7 of &e{total} &7perks. Total listed value: &e{spent}";
    private String perkOwnedLineMsg = "&a✔ &f{name} &7- &e{price}";
    private String perkUnownedLineMsg = "&c✖ &f{name} &7- &e{price}";
    private String footerPrevButtonMsg = "&e[<< Prev]";
    private String footerNextButtonMsg = "&e[Next >>]";
    private String footerPageInfoMsg = "&7Page &f{page}&7/&f{total_pages}";
    private String footerHintMsg = "&8| &7Info: &e/perks info <perk>";
    private String filterAllMsg = "All perks";
    private String filterOwnedMsg = "Owned";
    private String filterAvailableMsg = "Available";
    private String infoHeaderMsg = "&6&l{name}";
    private String infoCategoryMsg = "&7Category: &f{category}";
    private String infoPriceMsg = "&7Price: &e{price}";
    private String infoStatusOwnedMsg = "&7Status: &aOwned";
    private String infoStatusUnownedMsg = "&7Status: &cNot owned";
    private String infoDescriptionHeaderMsg = "&7Description:";
    private String infoDescriptionLineMsg = "&f  {line}";
    private String infoBuyHintMsg = "&7Use &e/perks &7and click the perk to buy.";
    private String infoOwnedHintMsg = "&7You already own this perk.";
    private String checkHeaderMsg = "&6&l{player}&7's perks";
    private String checkSummaryMsg = "&7Owns &a{owned}&7 of &e{total} &7perks. Total listed value: &e{spent}";
    private List<String> helpMsg = List.of(
            "&6&lPerkMenu Help",
            "&e/perks &7- Browse all perks",
            "&e/perks <page> &7- Jump to a specific page",
            "&e/perks owned &7- Perks you own",
            "&e/perks available &7- Perks you don't own yet",
            "&e/perks info <perk> &7- Detailed info about a perk",
            "&e/perks check <player> &7- Check another player's perks",
            "&e/perks reload &7- Reload the configuration",
            "&e/perks help &7- Show this help"
    );

    public void loadFromConfig(ConfigurationSection config) {
        perksPerPage = config.getInt("perks_per_page", perksPerPage);
        buyUrlTemplate = config.getString("buy_url_template", buyUrlTemplate);
        showOwnedInList = config.getBoolean("show_owned_in_list", showOwnedInList);

        ConfigurationSection messages = config.getConfigurationSection("messages");
        if (messages == null) return;

        noPermissionMsg = messages.getString("no_permission", noPermissionMsg);
        playersOnlyMsg = messages.getString("players_only", playersOnlyMsg);
        unknownCommandMsg = messages.getString("unknown_command", unknownCommandMsg);
        reloadedMsg = messages.getString("reloaded", reloadedMsg);
        noPerksMsg = messages.getString("no_perks", noPerksMsg);
        unknownPerkMsg = messages.getString("unknown_perk", unknownPerkMsg);
        invalidPageMsg = messages.getString("invalid_page", invalidPageMsg);
        listHeaderMsg = messages.getString("list_header", listHeaderMsg);
        summaryMsg = messages.getString("summary", summaryMsg);
        perkOwnedLineMsg = messages.getString("perk_owned_line", perkOwnedLineMsg);
        perkUnownedLineMsg = messages.getString("perk_unowned_line", perkUnownedLineMsg);
        footerPrevButtonMsg = messages.getString("footer_prev_button", footerPrevButtonMsg);
        footerNextButtonMsg = messages.getString("footer_next_button", footerNextButtonMsg);
        footerPageInfoMsg = messages.getString("footer_page_info", footerPageInfoMsg);
        footerHintMsg = messages.getString("footer_hint", footerHintMsg);
        filterAllMsg = messages.getString("filter_all", filterAllMsg);
        filterOwnedMsg = messages.getString("filter_owned", filterOwnedMsg);
        filterAvailableMsg = messages.getString("filter_available", filterAvailableMsg);
        infoHeaderMsg = messages.getString("info_header", infoHeaderMsg);
        infoCategoryMsg = messages.getString("info_category", infoCategoryMsg);
        infoPriceMsg = messages.getString("info_price", infoPriceMsg);
        infoStatusOwnedMsg = messages.getString("info_status_owned", infoStatusOwnedMsg);
        infoStatusUnownedMsg = messages.getString("info_status_unowned", infoStatusUnownedMsg);
        infoDescriptionHeaderMsg = messages.getString("info_description_header", infoDescriptionHeaderMsg);
        infoDescriptionLineMsg = messages.getString("info_description_line", infoDescriptionLineMsg);
        infoBuyHintMsg = messages.getString("info_buy_hint", infoBuyHintMsg);
        infoOwnedHintMsg = messages.getString("info_owned_hint", infoOwnedHintMsg);
        checkHeaderMsg = messages.getString("check_header", checkHeaderMsg);
        checkSummaryMsg = messages.getString("check_summary", checkSummaryMsg);

        List<String> help = messages.getStringList("help");
        if (!help.isEmpty()) {
            helpMsg = List.copyOf(help);
        }
    }

    public int getPerksPerPage() {
        return perksPerPage;
    }

    public String getBuyUrlTemplate() {
        return buyUrlTemplate;
    }

    public boolean isShowOwnedInList() {
        return showOwnedInList;
    }

    public String getNoPermissionMsg() { return noPermissionMsg; }
    public String getPlayersOnlyMsg() { return playersOnlyMsg; }
    public String getUnknownCommandMsg() { return unknownCommandMsg; }
    public String getReloadedMsg() { return reloadedMsg; }
    public String getNoPerksMsg() { return noPerksMsg; }
    public String getUnknownPerkMsg() { return unknownPerkMsg; }
    public String getInvalidPageMsg() { return invalidPageMsg; }
    public String getListHeaderMsg() { return listHeaderMsg; }
    public String getSummaryMsg() { return summaryMsg; }
    public String getPerkOwnedLineMsg() { return perkOwnedLineMsg; }
    public String getPerkUnownedLineMsg() { return perkUnownedLineMsg; }
    public String getFooterPrevButtonMsg() { return footerPrevButtonMsg; }
    public String getFooterNextButtonMsg() { return footerNextButtonMsg; }
    public String getFooterPageInfoMsg() { return footerPageInfoMsg; }
    public String getFooterHintMsg() { return footerHintMsg; }
    public String getFilterAllMsg() { return filterAllMsg; }
    public String getFilterOwnedMsg() { return filterOwnedMsg; }
    public String getFilterAvailableMsg() { return filterAvailableMsg; }
    public String getInfoHeaderMsg() { return infoHeaderMsg; }
    public String getInfoCategoryMsg() { return infoCategoryMsg; }
    public String getInfoPriceMsg() { return infoPriceMsg; }
    public String getInfoStatusOwnedMsg() { return infoStatusOwnedMsg; }
    public String getInfoStatusUnownedMsg() { return infoStatusUnownedMsg; }
    public String getInfoDescriptionHeaderMsg() { return infoDescriptionHeaderMsg; }
    public String getInfoDescriptionLineMsg() { return infoDescriptionLineMsg; }
    public String getInfoBuyHintMsg() { return infoBuyHintMsg; }
    public String getInfoOwnedHintMsg() { return infoOwnedHintMsg; }
    public String getCheckHeaderMsg() { return checkHeaderMsg; }
    public String getCheckSummaryMsg() { return checkSummaryMsg; }
    public List<String> getHelpMsg() { return helpMsg; }
}