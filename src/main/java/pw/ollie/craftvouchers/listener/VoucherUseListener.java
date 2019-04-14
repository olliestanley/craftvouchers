package pw.ollie.craftvouchers.listener;

import pw.ollie.craftvouchers.CraftVouchersPlugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public final class VoucherUseListener implements Listener {
    private final CraftVouchersPlugin plugin;

    public VoucherUseListener(CraftVouchersPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // todo
    }
}
