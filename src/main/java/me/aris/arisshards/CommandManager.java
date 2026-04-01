package me.aris.arisshards;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandManager implements CommandExecutor, TabCompleter {
    private final ArisShards plugin;

    public CommandManager(ArisShards plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) return true;
            Player p = (Player) sender;
            int bal = plugin.getDataManager().getShards(p.getUniqueId());
            p.sendMessage(plugin.getMessageManager().getMessage("balance-message").replace("%amount%", String.valueOf(bal)));
            return true;
        }

        if (!sender.hasPermission("arisshards.admin")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getMessageManager().getMessage("usage"));
            return true;
        }

        String action = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getMessageManager().getMessage("player-offline").replace("%player%", args[1]));
            return true;
        }

        int amount = 1;
        if (args.length == 3) {
            try { amount = Integer.parseInt(args[2]); } 
            catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessageManager().getMessage("usage"));
                return true;
            }
        }

        switch (action) {
            case "give":
                plugin.getDataManager().setShards(target.getUniqueId(), plugin.getDataManager().getShards(target.getUniqueId()) + amount);
                sender.sendMessage(plugin.getMessageManager().getMessage("give-success").replace("%player%", target.getName()).replace("%amount%", String.valueOf(amount)));
                break;
            case "take":
                plugin.getDataManager().setShards(target.getUniqueId(), Math.max(0, plugin.getDataManager().getShards(target.getUniqueId()) - amount));
                sender.sendMessage(plugin.getMessageManager().getMessage("take-success").replace("%player%", target.getName()).replace("%amount%", String.valueOf(amount)));
                break;
            case "reset":
                plugin.getDataManager().setShards(target.getUniqueId(), 0);
                sender.sendMessage(plugin.getMessageManager().getMessage("reset-success").replace("%player%", target.getName()));
                break;
            default:
                sender.sendMessage(plugin.getMessageManager().getMessage("usage"));
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (!sender.hasPermission("arisshards.admin")) return list;
        if (args.length == 1) {
            for (String s : Arrays.asList("give", "take", "reset")) {
                if (s.startsWith(args[0].toLowerCase())) list.add(s);
            }
        } else if (args.length == 2) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) list.add(p.getName());
            }
        }
        return list;
    }
}
