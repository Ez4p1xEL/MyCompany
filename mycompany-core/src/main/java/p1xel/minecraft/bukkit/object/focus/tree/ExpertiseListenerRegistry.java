package p1xel.minecraft.bukkit.object.focus.tree;

import org.bukkit.plugin.java.JavaPlugin;
import p1xel.minecraft.bukkit.object.effect.EffectType;
import p1xel.minecraft.bukkit.object.focus.tree.listener.*;
import p1xel.minecraft.bukkit.util.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;

public class ExpertiseListenerRegistry {

    private final JavaPlugin plugin;
    private final HashSet<ExpertiseListener> registeredListeners = new HashSet<>();
    private final HashMap<EffectType, ExpertiseListener> listenerMap = new HashMap<>();

    public ExpertiseListenerRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void bind(ExpertiseListener listener) {

        EffectType effectType = listener.getEffectType();

        // 将 Listener 放入 HashSet，确保没有重复注册
        if (registeredListeners.add(listener)) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
            Logger.log(Level.INFO, "Registered ExpertiseListener: " + listener.getName());
        }

        // 将 Listener 与 EffectType 关联
        // 建立索引
        listenerMap.put(effectType, listener);

    }

    public void init() {

        bind(new MobDropListener("mob_drop_listener", EffectType.MOB_DROPS_ADD));
        bind(new MobDamageListener("mob_damage_listener", EffectType.MOB_DAMAGE_MULTIPLY));

    }

}
