package me.aris.arisshards;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class MessageManager {
    private final ArisShards plugin;
    private FileConfiguration msgConfig;

    public MessageManager(ArisShards plugin) {
        this.plugin = plugin;
        File file = new File(plugin.getDataFolder(), "message.yml");
        if (!file.exists()) plugin.saveResource("message.yml", false);
        this.msgConfig = YamlConfiguration.loadConfiguration(file);
    }

    public String getMessage(String path) {
        String prefix = plugin.getConfig().getString("prefix", "");
        return ColorUtils.color(prefix + msgConfig.getString(path, ""));
    }

    public String getMessageOnly(String path) {
        return ColorUtils.color(msgConfig.getString(path, ""));
    }
}
