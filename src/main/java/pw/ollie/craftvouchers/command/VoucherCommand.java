package pw.ollie.craftvouchers.command;

import pw.ollie.craftvouchers.CraftVouchersPlugin;
import pw.ollie.craftvouchers.util.Util;
import pw.ollie.craftvouchers.voucher.QueuedVoucherCode;
import pw.ollie.craftvouchers.voucher.Voucher;

import com.google.common.base.Joiner;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

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
            if (plugin.getVoucherManager().getVoucher(name) != null) {
                sender.sendMessage(ChatColor.RED + "There is already a voucher with that name.");
                return true;
            }
            String itemTitle = Joiner.on(" ").join(Util.subArray(args, 2, args.length));
            if (plugin.getVoucherManager().getVoucherByItemTitle(itemTitle) != null) {
                sender.sendMessage(ChatColor.RED + "There is already a voucher with that item title.");
                return true;
            }
            plugin.getVoucherManager().addVoucher(name, itemTitle);
            sender.sendMessage(ChatColor.GRAY + "Successfully created new voucher.");
        } else if (subcommand.equals("give")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Please specify the name of the player and of the voucher.");
                return true;
            }

            String playerName = args[1];
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);
            if (!player.hasPlayedBefore()) {
                sender.sendMessage(ChatColor.RED + "That player has never played on the server before.");
                return true;
            }

            String voucherName = args[2];
            Voucher voucher = plugin.getVoucherManager().getVoucher(voucherName);
            if (voucher == null) {
                sender.sendMessage(ChatColor.RED + "That is not a valid voucher name.");
                return true;
            }

            String code = generateCode();
            voucher.addCode(code);
            if (player.isOnline()) {
                Player online = (Player) player;
                online.getInventory().addItem(voucher.getBook(code));
                sender.sendMessage(ChatColor.GRAY + "Successfully gave voucher.");
            } else {
                QueuedVoucherCode queued = new QueuedVoucherCode(player.getUniqueId(), voucherName, code);
                plugin.getVoucherManager().addQueued(queued);
                sender.sendMessage(ChatColor.GRAY + "The player will be given the voucher when they log in.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid subcommand: " + subcommand + " (valid: add, create, give)");
        }

        return true;
    }

    private String generateCode() {
        return UUID.randomUUID().toString();
    }
}
