package pw.ollie.craftvouchers.command;

import pw.ollie.craftvouchers.CraftVouchersPlugin;
import pw.ollie.craftvouchers.util.Util;
import pw.ollie.craftvouchers.voucher.Voucher;

import com.google.common.base.Joiner;

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

        String subcommand = args[0].toLowerCase();
        if (subcommand.equals("add")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Please specify the name of the voucher to add to and the command to add.");
                return true;
            }

            String name = args[1];
            Voucher voucher = plugin.getVoucherManager().getVoucher(name);
            if (voucher == null) {
                sender.sendMessage(ChatColor.RED + "There is no voucher with that name.");
                return true;
            }

            String command = Joiner.on(" ").join(Util.subArray(args, 2, args.length));
            voucher.addInstruction(command);
            sender.sendMessage(ChatColor.GRAY + "Successfully added new command to voucher.");
        } else if (subcommand.equals("create")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Please specify a name for the voucher and a title for voucher items.");
                return true;
            }

            String name = args[1];
            String itemTitle = Joiner.on(" ").join(Util.subArray(args, 2, args.length));
            plugin.getVoucherManager().addVoucher(name, itemTitle);
            sender.sendMessage(ChatColor.GRAY + "Successfully created new voucher.");
        } else if (subcommand.equals("give")) {
            // todo
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid subcommand: " + subcommand + " (valid: add, create, give)");
        }

        return true;
    }
}
