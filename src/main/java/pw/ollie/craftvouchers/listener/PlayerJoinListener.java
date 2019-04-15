package pw.ollie.craftvouchers.listener;

import pw.ollie.craftvouchers.CraftVouchersPlugin;
import pw.ollie.craftvouchers.voucher.QueuedVoucherCode;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public final class PlayerJoinListener implements Listener {
    private final CraftVouchersPlugin plugin;

    public PlayerJoinListener(CraftVouchersPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        for (QueuedVoucherCode queued : plugin.getVoucherManager().getCodeQueue()) {
            if (!queued.getPlayerId().equals(playerId)) {
                continue;
            }

            // todo: give player the voucher
        }
    }
}
