package p1xel.minecraft.bukkit.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.UUID;

public class TaxCollectEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final List<UUID> companies;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public TaxCollectEvent(List<UUID> companies) {
        this.companies = companies;
        this.cancelled = false;
    }

    public List<UUID> getCompanies() {
        return this.companies;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean bool) {
        this.cancelled = bool;
    }
}
