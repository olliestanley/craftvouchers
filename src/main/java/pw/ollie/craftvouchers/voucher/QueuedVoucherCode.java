package pw.ollie.craftvouchers.voucher;

import org.bson.BSONObject;
import org.bson.BasicBSONObject;

import java.util.UUID;

public final class QueuedVoucherCode {
    private final UUID playerId;
    private final String voucherName;
    private final String code;

    public QueuedVoucherCode(UUID playerId, String voucherName, String code) {
        this.playerId = playerId;
        this.voucherName = voucherName;
        this.code = code;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getVoucherName() {
        return voucherName;
    }

    public String getCode() {
        return code;
    }

    public BSONObject toBSONObject() {
        BSONObject result = new BasicBSONObject();
        result.put("player", playerId.toString());
        result.put("voucher", voucherName);
        result.put("code", code);
        return result;
    }

    public static QueuedVoucherCode fromBSONObject(BasicBSONObject bObj) {
        return new QueuedVoucherCode(UUID.fromString(bObj.getString("player")), bObj.getString("voucher"), bObj.getString("code"));
    }
}
