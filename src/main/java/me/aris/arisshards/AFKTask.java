package me.aris.arisshards;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AFKTask {
    private final ArisShards plugin;
    private final Map<UUID, Integer> timers = new HashMap<>();
    private final Set<UUID> inRegion = new HashSet<>();

    public AFKTask(ArisShards plugin) { this.plugin = plugin; }

    public void start() {
        Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                boolean currentlyIn = isInRegion(player);
                if (currentlyIn) {
                    if (!inRegion.contains(uuid)) {
                        inRegion.add(uuid);
                        String sub = plugin.getMessageManager().getMessageOnly("enter-region-subtitle");
                        player.sendTitle("", ColorUtils.color(sub), 10, 40, 10);
                        playSound(player, "enter-region");
                    }
                    int current = timers.getOrDefault(uuid, 0) + 1;
                    timers.put(uuid, current);
                    int timeLeft = getTimeLeft(player, current);
                    sendActionBar(player, timeLeft);
                    if (timeLeft <= 0) {
                        giveReward(player);
                        timers.put(uuid, 0);
                    }
                } else if (inRegion.contains(uuid)) {
                    inRegion.remove(uuid);
                    timers.remove(uuid);
                    String sub = plugin.getMessageManager().getMessageOnly("exit-region-subtitle");
                    player.sendTitle("", ColorUtils.color(sub), 10, 40, 10);
                    playSound(player, "exit-region");
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, ColorUtils.getFormatted(""));
                }
            }
        }, 1, 1, java.util.concurrent.TimeUnit.SECONDS);
    }

    private boolean isInRegion(Player player) {
        List<String> regions = plugin.getConfig().getStringList("regions");
        if (regions == null) return false;
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            return query.getApplicableRegions(BukkitAdapter.adapt(player.getLocation())).getRegions()
                    .stream().anyMatch(r -> regions.contains(r.getId()));
        } catch (Exception e) { return false; }
    }

    private int getTimeLeft(Player player, int current) {
        ConfigurationSection tiers = plugin.getConfig().getConfigurationSection("tiers");
        if (tiers == null) return 0;
        for (String key : tiers.getKeys(false)) {
            if (player.hasPermission(tiers.getString(key + ".permission"))) {
                return Math.max(0, tiers.getInt(key + ".time") - current);
            }
        }
        return 0;
    }

    private void giveReward(Player player) {
        ConfigurationSection tiers = plugin.getConfig().getConfigurationSection("tiers");
        if (tiers == null) return;
        for (String key : tiers.getKeys(false)) {
            if (player.hasPermission(tiers.getString(key + ".permission"))) {
                int amount = tiers.getInt(key + ".amount");
                plugin.getDataManager().setShards(player.getUniqueId(), plugin.getDataManager().getShards(player.getUniqueId()) + amount);
                String sub = plugin.getMessageManager().getMessageOnly("receive-subtitle").replace("%amount%", String.valueOf(amount));
                player.sendTitle("", ColorUtils.color(sub), 10, 40, 10);
                playSound(player, "receive-shards");
                break;
            }
        }
    }

    private void playSound(Player player, String configPath) {
        String soundName = plugin.getConfig().getString("sounds." + configPath);
        if (soundName != null) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(soundName), 1.0f, 1.0f);
            } catch (Exception ignored) {}
        }
    }

    private void sendActionBar(Player player, int time) {
        String msg = plugin.getMessageManager().getMessageOnly("actionbar-afk").replace("%time%", String.valueOf(time));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, ColorUtils.getFormatted(msg));
    }
                        }
