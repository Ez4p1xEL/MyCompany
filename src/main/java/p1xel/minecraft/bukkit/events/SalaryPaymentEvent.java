package p1xel.minecraft.bukkit.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.UUID;

public class SalaryPaymentEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private List<UUID> companies;

    public SalaryPaymentEvent(List<UUID> companies) {
        this.companies = companies;
        this.cancelled = false;
    }

    public List<UUID> getCompanies() {
        return companies;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean bool) {
        cancelled = bool;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
