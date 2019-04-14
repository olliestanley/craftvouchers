package pw.ollie.craftvouchers.voucher;

import pw.ollie.craftvouchers.CraftVouchersPlugin;

import org.bson.BSONDecoder;
import org.bson.BSONObject;
import org.bson.BasicBSONDecoder;
import org.bson.types.BasicBSONList;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class VoucherManager {
    private final CraftVouchersPlugin plugin;
    private final Map<String, Voucher> vouchers;

    public VoucherManager(CraftVouchersPlugin plugin) {
        this.plugin = plugin;
        this.vouchers = new HashMap<>();
    }

    public Voucher getVoucher(String name) {
        return vouchers.get(name);
    }

    public void loadVouchers() {
        File dataFolder = plugin.getDataFolder();
        dataFolder.mkdirs();
        plugin.saveResource("vouchers.yml", false);

        File vouchersConfigFile = new File(dataFolder, "vouchers.yml");
        YamlConfiguration vouchersConfig = YamlConfiguration.loadConfiguration(vouchersConfigFile);

        for (String voucherKey : vouchersConfig.getKeys(false)) {
            ConfigurationSection voucherSection = vouchersConfig.getConfigurationSection(voucherKey);
            if (voucherSection == null) {
                plugin.getLogger().log(Level.WARNING, "Invalid voucher configuration entry: " + voucherKey);
                continue;
            }

            String name = voucherSection.getString("name", voucherKey);
            List<String> instructions = voucherSection.getStringList("instructions");
            this.vouchers.put(name, new Voucher(name, instructions));
        }

        File codesFile = new File(dataFolder, "codes.bson");
        if (!codesFile.exists()) {
            return;
        }

        try {
            byte[] codesData = Files.readAllBytes(codesFile.toPath());
            BSONDecoder decoder = new BasicBSONDecoder();
            BSONObject bObj = decoder.readObject(codesData);

            for (String voucherName : bObj.keySet()) {
                Voucher voucher = vouchers.get(voucherName);
                if (voucher == null) {
                    continue;
                }

                ((BasicBSONList) bObj.get(voucherName))
                        .stream()
                        .map(Object::toString)
                        .collect(Collectors.toSet())
                        .forEach(voucher::addCode);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "ERROR: Could not read codes data!", e);
        }
    }

    public void saveVouchers() {
        // todo
    }
}
