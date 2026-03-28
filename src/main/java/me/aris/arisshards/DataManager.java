package me.aris.arisshards;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    private final ArisShards plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<UUID, Integer> playerShards = new HashMap<>();

    public DataManager(ArisShards plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
    }

    public void loadData() {
        config = YamlConfiguration.loadConfiguration(file);
        if (config.contains("shards")) {
            for (String key : config.getConfigurationSection("shards").getKeys(false)) {
                playerShards.put(UUID.fromString(key), config.getInt("shards." + key));
            }
        }
    }

    public void saveData() {
        config = new YamlConfiguration();
        for (Map.Entry<UUID, Integer> entry : playerShards.entrySet()) {
            config.set("shards." + entry.getKey().toString(), entry.getValue());
        }
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public int getShards(UUID uuid) { return playerShards.getOrDefault(uuid, 0); }
    public void setShards(UUID uuid, int amount) { playerShards.put(uuid, amount); }
      }
