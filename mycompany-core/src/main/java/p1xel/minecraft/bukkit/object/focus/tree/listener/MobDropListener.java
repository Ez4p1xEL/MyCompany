package p1xel.minecraft.bukkit.object.focus.tree.listener;

import org.bukkit.event.EventHandler;
import p1xel.minecraft.bukkit.object.focus.tree.ExpertiseListener;

public class MobDropListener extends ExpertiseListener {

    public MobDropListener(String name, String... expertiseIds) {
        super(name, expertiseIds);
    }

    @EventHandler
    public void aaa() {
        // 处理事件的逻辑
    }

}
