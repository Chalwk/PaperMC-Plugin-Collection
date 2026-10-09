// Copyright (c) 2026. Jericho Crosby (Chalwk)

package com.chalwk.util;

import com.chalwk.VacuLoot;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyHelper {

    private final VacuLoot plugin;
    private Economy economy;

    public EconomyHelper(VacuLoot plugin) {
        this.plugin = plugin;
    }

    public boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null)
            return false;

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;

        economy = rsp.getProvider();
        return true;
    }

    public boolean hasEnough(Player player, double amount) {
        if (economy == null)
            return true;
        return economy.has(player, amount);
    }

    public void withdraw(Player player, double amount) {
        if (economy != null)
            economy.withdrawPlayer(player, amount);
    }
}