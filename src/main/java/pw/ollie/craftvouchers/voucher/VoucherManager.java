package pw.ollie.craftvouchers.voucher;

import pw.ollie.craftvouchers.CraftVouchersPlugin;
import pw.ollie.craftvouchers.util.Util;

import org.bson.BSONDecoder;
import org.bson.BSONEncoder;
import org.bson.BSONObject;
import org.bson.BasicBSONDecoder;
import org.bson.BasicBSONEncoder;
import org.bson.BasicBSONObject;
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
        File dataFolder = plugin.getDataFolder();
        dataFolder.mkdirs();

        File vouchersConfigFile = new File(dataFolder, "vouchers.yml");
        YamlConfiguration vouchersConfig = YamlConfiguration.loadConfiguration(vouchersConfigFile);

        for (Voucher voucher : vouchers.values()) {
            ConfigurationSection voucherSection = Util.getOrCreateSection(vouchersConfig, voucher.getName());
            voucherSection.set("Name", voucher.getName());
            voucherSection.set("Commands", voucher.getInstructions());
        }

        for (String vouchersKey : vouchersConfig.getKeys(false)) {
            if (!vouchers.containsKey(vouchersKey)) {
                vouchersConfig.set(vouchersKey, null);
            }
        }

        try {
            vouchersConfig.save(vouchersConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save updated vouchers...", e);
        }

        this.saveCodeData();
    }

    public void saveCodeData() {
        File dataFolder = plugin.getDataFolder();
        File codesFile = new File(dataFolder, "codes.bson");
        File backupFile = new File(dataFolder, "codes.bson.bck");
        if (!codesFile.exists()) {
            try {
                codesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "ERROR: Could not save codes data!", e);
                return;
            }
        } else {
            try {
                backupFile.delete();
                Files.copy(codesFile.toPath(), backupFile.toPath());
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "ERROR: Cannot save codes data as backup could not be made!", e);
                return;
            }
        }

        BSONObject bObj = new BasicBSONObject();
        for (Voucher voucher : vouchers.values()) {
            BasicBSONList bList = new BasicBSONList();
            bList.addAll(voucher.getValidCodes());
            bObj.put(voucher.getName(), bList);
        }

        BSONEncoder encoder = new BasicBSONEncoder();
        byte[] data = encoder.encode(bObj);
        try {
            Files.write(codesFile.toPath(), data);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "ERROR: Could not save codes data, attempting to restore backup...", e);

            try {
                Files.copy(backupFile.toPath(), codesFile.toPath());
            } catch (IOException e1) {
                plugin.getLogger().log(Level.SEVERE, "Could not restore codes data backup, restore manually...");
            }
        }
    }
}
