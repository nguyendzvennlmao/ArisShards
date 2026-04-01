package me.aris.arisshards;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class ShardsExpansion extends PlaceholderExpansion {
    private final ArisShards plugin;

    public ShardsExpansion(ArisShards plugin) { this.plugin = plugin; }

    @Override
    public String getIdentifier() { return "arisshards"; }

    @Override
    public String getAuthor() { return "VennLMAO"; }

    @Override
    public String getVersion() { return "1.0"; }

    @Override
    public boolean persist() { return true; }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) return "0";
        if (params.equalsIgnoreCase("balance")) {
            return format(plugin.getDataManager().getShards(player.getUniqueId()));
        }
        return null;
    }

    private String format(int n) {
        if (n < 1000) return String.valueOf(n);
        String k = plugin.getConfig().getString("amount-format.k", "K");
        String m = plugin.getConfig().getString("amount-format.m", "M");
        if (n < 1000000) return (n / 1000) + k;
        return (n / 1000000) + m;
    }
}
