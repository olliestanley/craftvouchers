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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class VoucherManager {
    private final CraftVouchersPlugin plugin;
    private final Map<String, Voucher> vouchers;
    private final Set<QueuedVoucherCode> queue;

    public VoucherManager(CraftVouchersPlugin plugin) {
        this.plugin = plugin;
        this.vouchers = new HashMap<>();
        this.queue = new HashSet<>();
    }

    public Voucher getVoucher(String name) {
        return vouchers.get(name.toLowerCase());
    }

    public Voucher getVoucherByItemTitle(String itemTitle) {
        for (Voucher voucher : vouchers.values()) {
            if (voucher.getItemTitle().equals(itemTitle)) {
                return voucher;
            }
        }
        return null;
    }

    public Voucher getVoucherByCode(String code) {
        for (Voucher voucher : vouchers.values()) {
            if (voucher.isValidCode(code)) {
                return voucher;
            }
        }
        return null;
    }

    public Set<Voucher> getVouchers() {
        return new HashSet<>(vouchers.values());
    }

    public void addVoucher(String name, String itemTitle) {
        vouchers.put(name.toLowerCase(), new Voucher(name, itemTitle));
    }

    public boolean removeVoucher(String name) {
        return vouchers.remove(name.toLowerCase()) != null;
    }

    public Set<QueuedVoucherCode> getCodeQueue() {
        return new HashSet<>(queue);
    }

    public void removeQueued(QueuedVoucherCode code) {
        queue.remove(code);
    }

    public void addQueued(QueuedVoucherCode code) {
        queue.add(code);
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

            String name = voucherSection.getString("Name", voucherKey);
            String itemTitle = voucherSection.getString("Item-Title", name);
            List<String> instructions = voucherSection.getStringList("Commands");
            this.vouchers.put(name.toLowerCase(), new Voucher(name, itemTitle, instructions));
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

        File queueFile = new File(dataFolder, "queue.bson");
        if (!queueFile.exists()) {
            return;
        }

        try {
            byte[] queueData = Files.readAllBytes(codesFile.toPath());
            if (queueData.length != 0) {
                BSONDecoder decoder = new BasicBSONDecoder();
                BSONObject bObj = decoder.readObject(queueData);

                for (String voucherCode : bObj.keySet()) {
                    Object codeObjBase = bObj.get(voucherCode);
                    if (codeObjBase instanceof BasicBSONObject) {
                        BasicBSONObject codeObj = (BasicBSONObject) codeObjBase;
                        String name = codeObj.getString("name");
                        String code = codeObj.getString("code");
                        UUID playerId = UUID.fromString(codeObj.getString("player"));
                        queue.add(new QueuedVoucherCode(playerId, name, code));
                    }
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "ERROR: Could not read queue data!", e);
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
            voucherSection.set("Item-Title", voucher.getItemTitle());
            voucherSection.set("Commands", voucher.getInstructions());
        }

        for (String vouchersKey : vouchersConfig.getKeys(false)) {
            if (!vouchers.containsKey(vouchersKey.toLowerCase())) {
                vouchersConfig.set(vouchersKey, null);
            }
        }

        try {
            vouchersConfig.save(vouchersConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save updated vouchers...", e);
        }

        this.saveCodeData();
        this.saveGiveQueue();
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

    public void saveGiveQueue() {
        File dataFolder = plugin.getDataFolder();
        File queueFile = new File(dataFolder, "queue.bson");
        File backupFile = new File(dataFolder, "queue.bson.bck");
        if (!queueFile.exists()) {
            try {
                queueFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "ERROR: Could not save queue data!", e);
                return;
            }
        } else {
            try {
                backupFile.delete();
                Files.copy(queueFile.toPath(), backupFile.toPath());
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "ERROR: Cannot save queue data as backup could not be made!", e);
                return;
            }
        }

        BasicBSONObject bObj = new BasicBSONObject();
        for (QueuedVoucherCode queued : queue) {
            BasicBSONObject codeObj = new BasicBSONObject();
            codeObj.put("player", queued.getPlayerId().toString());
            codeObj.put("voucher", queued.getVoucherName());
            codeObj.put("code", queued.getCode());
            bObj.put(queued.getCode(), codeObj);
        }

        BSONEncoder encoder = new BasicBSONEncoder();
        byte[] data = bObj.isEmpty() ? new byte[0] : encoder.encode(bObj);
        try {
            Files.write(queueFile.toPath(), data);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "ERROR: Could not save queue data, attempting to restore backup...", e);

            try {
                Files.copy(backupFile.toPath(), queueFile.toPath());
            } catch (IOException e1) {
                plugin.getLogger().log(Level.SEVERE, "Could not restore queue data backup, restore manually...");
            }
        }
    }
}
