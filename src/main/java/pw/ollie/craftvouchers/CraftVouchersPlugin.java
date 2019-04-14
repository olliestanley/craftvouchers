package pw.ollie.craftvouchers;

import pw.ollie.craftvouchers.command.VoucherCommand;
import pw.ollie.craftvouchers.voucher.VoucherManager;

import org.bukkit.plugin.java.JavaPlugin;

public final class CraftVouchersPlugin extends JavaPlugin {
    private VoucherManager voucherManager;

    @Override
    public void onEnable() {
        this.voucherManager = new VoucherManager(this);
        this.voucherManager.loadVouchers();

        this.getCommand("voucher").setExecutor(new VoucherCommand(this));
    }

    @Override
    public void onDisable() {
        this.voucherManager.saveVouchers();
    }

    public VoucherManager getVoucherManager() {
        return voucherManager;
    }
}
