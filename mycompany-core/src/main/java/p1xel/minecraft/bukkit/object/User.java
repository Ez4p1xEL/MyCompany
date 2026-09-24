package p1xel.minecraft.bukkit.object;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public class User {

    // 必填欄位
    private final UUID playerUniqueId;

    // 可選 / 未來擴充欄位（賦予預設值）
    private @Nullable UUID companyUniqueId;
    private @Nullable String position;
    private HashMap<String, EmployeeOrder> employeeOrders = new HashMap<>();

    // 私有構造器，僅允許通過 Builder 實例化
    private User(Builder builder) {
        this.playerUniqueId = builder.playerUniqueId;
        this.companyUniqueId = builder.companyUniqueId;
        this.position = builder.position;
        this.employeeOrders = builder.employeeOrders;
    }

    // 靜態入口
    public static Builder builder(UUID uuid) {
        return new Builder(uuid);
    }

    // Getter & Setter ...
    public UUID getUUID() { return playerUniqueId; }
    public @Nullable UUID getCompanyUUID() { return companyUniqueId; }
    public void setCompanyUUID(UUID companyUniqueId) { this.companyUniqueId = companyUniqueId; }
    public @Nullable String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public EmployeeOrder getEmployeeOrder(String orderId) { return employeeOrders.get(orderId); }
    public void createOrder(String orderId, EmployeeOrder order) { this.employeeOrders.put(orderId, order); }
    public void removeOrder(String orderId) { this.employeeOrders.remove(orderId); }

    // ---------------- Builder 內部類 ----------------
    public static class Builder {
        private final UUID playerUniqueId;
        private UUID companyUniqueId = null;
        private String position = null;
        private HashMap<String, EmployeeOrder> employeeOrders = new HashMap<>();

        public Builder(UUID playerUniqueId) {
            this.playerUniqueId = Objects.requireNonNull(playerUniqueId, "UUID cannot be null");
        }

        public Builder companyUniqueId(@Nullable UUID companyUniqueId) {
            this.companyUniqueId = companyUniqueId;
            return this;
        }

        public Builder position(@Nullable String position) {
            this.position = position;
            return this;
        }

        public Builder employeeOrders(HashMap<String, EmployeeOrder> employeeOrders) {
            this.employeeOrders = employeeOrders;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}