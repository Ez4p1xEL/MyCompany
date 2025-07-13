package p1xel.minecraft.bukkit;

import java.util.UUID;

public class Company {

    private final UUID uuid;

    public Company(UUID uniqueId) {
        uuid = uniqueId;
    }

    public UUID getUUID() {
        return uuid;
    }

}
