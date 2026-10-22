package p1xel.minecraft.bukkit.object.focus.tree;

import org.bukkit.event.Listener;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.manager.CacheManager;
import p1xel.minecraft.bukkit.manager.CompanyManager;
import p1xel.minecraft.bukkit.manager.UserManager;
import p1xel.minecraft.bukkit.object.effect.EffectType;
import p1xel.minecraft.bukkit.object.focus.FocusManager;

public class ExpertiseListener implements Listener {

    public final String name;
    public final EffectType effectType;

    public final CacheManager cacheManager;
    public final CompanyManager companyManager;
    public final UserManager userManager;
    public final FocusManager focusManager;

    public ExpertiseListener(String name, EffectType effectType) {
        this.name = name;
        this.effectType = effectType;
        this.cacheManager = MyCompany.getCacheManager();
        this.companyManager = cacheManager.getCompanyManager();
        this.userManager = cacheManager.getUserManager();
        this.focusManager = cacheManager.getFocusManager();
    }

    public String getName() {
        return name;
    }

    public EffectType getEffectType() {
        return effectType;
    }


}
