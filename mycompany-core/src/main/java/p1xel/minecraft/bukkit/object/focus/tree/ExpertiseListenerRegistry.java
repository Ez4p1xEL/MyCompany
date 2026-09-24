package p1xel.minecraft.bukkit.object.focus.tree;

import org.bukkit.plugin.java.JavaPlugin;
import p1xel.minecraft.bukkit.object.focus.tree.listener.*;

import java.util.HashMap;
import java.util.HashSet;

public class ExpertiseListenerRegistry {

    private final JavaPlugin plugin;
    private final HashSet<ExpertiseListener> registeredListeners = new HashSet<>();
    private final HashMap<String, ExpertiseListener> listenerMap = new HashMap<>();

    public ExpertiseListenerRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void bind(ExpertiseListener listener) {

        String[] expertiseIds = listener.getExpertiseIds();

        // 将 Listener 放入 HashSet，确保没有重复注册
        if (registeredListeners.add(listener)) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }

        // 将 Listener 与 Expertise ID 关联
        // 建立索引
        for (String expertiseId : expertiseIds) {
            listenerMap.put(expertiseId, listener);
        }

    }

    public void init() {

        bind(new MobDropListener("mob_drop_listener", "more_drops_level1"));

    }

}
