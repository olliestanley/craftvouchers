package pw.ollie.craftvouchers.voucher;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Voucher {
    private final String name;
    private final String itemTitle;
    private final List<String> instructions;
    private final Set<String> validCodes;

    public Voucher(String name, String itemTitle) {
        this(name, itemTitle, new ArrayList<>());
    }

    public Voucher(String name, String itemTitle, List<String> instructions) {
        this.name = name;
        this.itemTitle = itemTitle;
        this.instructions = instructions;
        this.validCodes = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public List<String> getInstructions() {
        return new ArrayList<>(instructions);
    }

    public void addInstruction(String instruction) {
        instructions.add(instruction);
    }

    public boolean isValidCode(String code) {
        return validCodes.contains(code);
    }

    public void removeCode(String code) {
        validCodes.remove(code);
    }

    public void addCode(String code) {
        validCodes.add(code);
    }

    public Set<String> getValidCodes() {
        return new HashSet<>(validCodes);
    }

    public ItemStack getBook(String code) {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            itemMeta = Bukkit.getItemFactory().getItemMeta(Material.WRITTEN_BOOK);
        }
        BookMeta bookMeta = (BookMeta) itemMeta;
        bookMeta.setTitle(itemTitle);
        bookMeta.setDisplayName(itemTitle);
        bookMeta.setLore(Collections.singletonList(code));
        item.setItemMeta(bookMeta);
        return item;
    }
}
