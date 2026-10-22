package p1xel.minecraft.bukkit.util;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.EntityType;
import p1xel.minecraft.bukkit.util.storage.Locale;

public class Translator {

    public static String translateLog(String logId, String type) {

        switch (logId) {
            case "mob_kill_count": {
                EntityType entityType = EntityType.valueOf(type.toUpperCase());
                return Locale.deserialize(Component.translatable(entityType.translationKey()));
            }

            default:
                return type;
        }


    }

}
