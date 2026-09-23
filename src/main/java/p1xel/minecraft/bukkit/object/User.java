package p1xel.minecraft.bukkit.object;

import java.util.UUID;

public class User {

    private final UUID playerUniqueId;

    public User(UUID playerUniqueId) {
        this.playerUniqueId = playerUniqueId;
    }

    public UUID getUUID() {
        return playerUniqueId;
    }

}
