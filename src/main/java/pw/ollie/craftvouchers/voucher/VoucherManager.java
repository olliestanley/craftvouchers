package pw.ollie.craftvouchers.voucher;

import pw.ollie.craftvouchers.CraftVouchersPlugin;

import java.util.HashMap;
import java.util.Map;

public final class VoucherManager {
    private final CraftVouchersPlugin plugin;
    private final Map<String, Voucher> vouchers;

    public VoucherManager(CraftVouchersPlugin plugin) {
        this.plugin = plugin;
        this.vouchers = new HashMap<>();
    }
}
