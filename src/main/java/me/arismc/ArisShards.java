package me.arismc;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArisShards extends JavaPlugin implements CommandExecutor {

    private final Map<UUID, Integer> playerTimers = new HashMap<>();
    private final Map<UUID, Long> shardData = new HashMap<>(); 
    private final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        // Đăng ký lệnh /shard
        if (getCommand("shard") != null) {
            getCommand("shard").setExecutor(this);
        }

        // Đăng ký PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ShardExpansion(this).register();
        }

        // Tối ưu Folia: Chạy Async đếm ngược mỗi giây
        Bukkit.getAsyncScheduler().runAtFixedRate(this, (task) -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                handleTicking(player);
            }
        }, 1L, 1L, TimeUnit.SECONDS);
    }

    public long getShards(UUID uuid) {
        return shardData.getOrDefault(uuid, 0L);
    }

    public void setShards(UUID uuid, long amount) {
        shardData.put(uuid, Math.max(0, amount));
    }

    public String format(String msg) {
        if (msg == null) return "";
        Matcher matcher = hexPattern.matcher(msg);
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    private void handleTicking(Player player) {
        if (!isInAllowedRegion(player)) {
            playerTimers.remove(player.getUniqueId());
            return;
        }

        ShardGroup group = getGroup(player);
        if (group == null) return;

        UUID uuid = player.getUniqueId();
        int timeLeft = playerTimers.getOrDefault(uuid, group.interval);
        timeLeft--;

        if (timeLeft <= 0) {
            setShards(uuid, getShards(uuid) + group.amount);
            
            sendActionBar(player, getConfig().getString("messages.actionbar-reward")
                    .replace("%amount%", String.valueOf(group.amount)));
            
            Bukkit.getRegionScheduler().execute(this, player.getLocation(), () -> {
                player.playSound(player.getLocation(), 
                        Sound.valueOf(getConfig().getString("reward-sound")), 1.0f, 1.0f);
            });
            
            timeLeft = group.interval;
        } else {
            sendActionBar(player, getConfig().getString("messages.actionbar-progress")
                    .replace("%time%", String.valueOf(timeLeft)));
        }

        playerTimers.put(uuid, timeLeft);
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(format(message)));
    }

    private ShardGroup getGroup(Player player) {
        ConfigurationSection section = getConfig().getConfigurationSection("groups");
        if (section == null) return null;
        for (String key : section.getKeys(false)) {
            if (player.hasPermission(section.getString(key + ".permission"))) {
                return new ShardGroup(section.getInt(key + ".interval"), section.getInt(key + ".amount"));
            }
        }
        return null;
    }

    private boolean isInAllowedRegion(Player player) {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) return false;
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));
        List<String> allowed = getConfig().getStringList("allowed-regions");
        return set.getRegions().stream().anyMatch(r -> allowed.contains(r.getId()));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("arisshards.admin")) {
            sender.sendMessage(format("&cBạn không có quyền!"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(format("&e/shard give/take/set <player> <amount>"));
            sender.sendMessage(format("&e/shard reload"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            sender.sendMessage(format("&aĐã reload cấu hình ArisShards!"));
            return true;
        }

        if (args.length < 3) return false;

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(format("&cNgười chơi không online!"));
            return true;
        }

        long amount;
        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(format("&cSố lượng phải là số!"));
            return true;
        }

        UUID uuid = target.getUniqueId();
        switch (args[0].toLowerCase()) {
            case "give" -> {
                setShards(uuid, getShards(uuid) + amount);
                sender.sendMessage(format("&aĐã tặng " + amount + " shards cho " + target.getName()));
            }
            case "take" -> {
                setShards(uuid, getShards(uuid) - amount);
                sender.sendMessage(format("&aĐã lấy " + amount + " shards từ " + target.getName()));
            }
            case "set" -> {
                setShards(uuid, amount);
                sender.sendMessage(format("&aĐã đặt shards của " + target.getName() + " thành " + amount));
            }
        }
        return true;
    }

    private static class ShardGroup {
        int interval, amount;
        ShardGroup(int i, int a) { this.interval = i; this.amount = a; }
    }
}
