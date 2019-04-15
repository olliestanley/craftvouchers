package pw.ollie.craftvouchers.listener;

import pw.ollie.craftvouchers.CraftVouchersPlugin;
import pw.ollie.craftvouchers.voucher.Voucher;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class VoucherUseListener implements Listener {
    private final CraftVouchersPlugin plugin;

    public VoucherUseListener(CraftVouchersPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        ItemStack itemStack = event.getItem();
        if (itemStack == null || itemStack.getType() != Material.BOOK) {
            return;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return;
        }

        List<String> lore = itemMeta.getLore();
        if (lore == null || lore.size() < 2) {
            return;
        }

        String firstLine = lore.get(1);
        for (ChatColor color : ChatColor.values()) {
            firstLine = firstLine.replace(color.toString(), "");
        }
        Voucher voucher = plugin.getVoucherManager().getVoucherByCode(firstLine);
        if (voucher == null || !voucher.isValidCode(firstLine)) {
            return;
        }

        Player player = event.getPlayer();
        player.sendMessage(ChatColor.GREEN + "Redeemed voucher!");
        voucher.getInstructions().forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("%player%", player.getName())));
        voucher.removeCode(firstLine);
        player.getInventory().remove(itemStack);
        event.setCancelled(true);
    }
}
