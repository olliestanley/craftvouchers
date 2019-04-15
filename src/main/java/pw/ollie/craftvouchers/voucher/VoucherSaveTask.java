package pw.ollie.craftvouchers.voucher;

import pw.ollie.craftvouchers.CraftVouchersPlugin;

import org.bukkit.scheduler.BukkitRunnable;

public final class VoucherSaveTask extends BukkitRunnable {
    private final CraftVouchersPlugin plugin;

    public VoucherSaveTask(CraftVouchersPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        VoucherManager voucherManager = plugin.getVoucherManager();

        voucherManager.saveCodeData();
        voucherManager.saveGiveQueue();
    }
}
