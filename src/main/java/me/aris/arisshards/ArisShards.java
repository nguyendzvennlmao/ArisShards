package me.aris.arisshards;

import org.bukkit.plugin.java.JavaPlugin;

public class ArisShards extends JavaPlugin {
    private DataManager dataManager;
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.messageManager = new MessageManager(this);
        this.dataManager = new DataManager(this);
        this.dataManager.loadData();

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new ShardsExpansion(this).register();
        }

        new AFKTask(this).start();

        CommandManager cm = new CommandManager(this);
        getCommand("arisshards").setExecutor(cm);
        getCommand("arisshards").setTabCompleter(cm);
    }

    @Override
    public void onDisable() {
        if (dataManager != null) dataManager.saveData();
    }

    public DataManager getDataManager() { return dataManager; }
    public MessageManager getMessageManager() { return messageManager; }
}
