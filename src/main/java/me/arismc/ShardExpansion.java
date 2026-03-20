package me.arismc;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShardExpansion extends PlaceholderExpansion {

    private final ArisShards plugin;

    public ShardExpansion(ArisShards plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "aris";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ArisMC";
    }

    @Override
    public @NotNull String getVersion() {
        return "6.3";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "0";

        // %aris_shards%
        if (params.equalsIgnoreCase("shards")) {
            return String.valueOf(plugin.getShards(player.getUniqueId()));
        }

        return null;
    }
}
