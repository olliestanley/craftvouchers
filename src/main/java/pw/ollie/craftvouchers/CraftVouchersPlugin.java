package pw.ollie.craftvouchers;

import pw.ollie.craftvouchers.command.VoucherCommand;
import pw.ollie.craftvouchers.listener.PlayerJoinListener;
import pw.ollie.craftvouchers.listener.VoucherUseListener;
import pw.ollie.craftvouchers.voucher.VoucherManager;
import pw.ollie.craftvouchers.voucher.VoucherSaveTask;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class CraftVouchersPlugin extends JavaPlugin {
    private VoucherManager voucherManager;
    private VoucherSaveTask saveTask;

    @Override
    public void onEnable() {
        this.voucherManager = new VoucherManager(this);
        this.voucherManager.loadVouchers();

        this.saveTask = new VoucherSaveTask(this);
        this.saveTask.runTaskTimer(this, 20 * 60 * 5, 20 * 60 * 5);

        this.getCommand("voucher").setExecutor(new VoucherCommand(this));

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerJoinListener(this), this);
        pluginManager.registerEvents(new VoucherUseListener(this), this);
    }

    @Override
    public void onDisable() {
        this.voucherManager.saveVouchers();
        this.saveTask.cancel();
    }

    public VoucherManager getVoucherManager() {
        return voucherManager;
    }
}
