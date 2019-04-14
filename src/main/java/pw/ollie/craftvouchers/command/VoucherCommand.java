package pw.ollie.craftvouchers.command;

import pw.ollie.craftvouchers.CraftVouchersPlugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class VoucherCommand implements CommandExecutor {
    private final CraftVouchersPlugin plugin;

    public VoucherCommand(CraftVouchersPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("craftvouchers.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /voucher <add/create/give> [args...]");
            return true;
        }
        return true;
    }
}
