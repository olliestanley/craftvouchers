package pw.ollie.craftvouchers.voucher;

import java.util.ArrayList;
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
}
