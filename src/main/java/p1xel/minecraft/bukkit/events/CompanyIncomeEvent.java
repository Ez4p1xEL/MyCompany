package p1xel.minecraft.bukkit.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class CompanyIncomeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final UUID uniqueId;
    private double amount;

    public CompanyIncomeEvent(UUID uniqueId, double amount) {
        this.uniqueId = uniqueId;
        this.amount = amount;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public double getAmount() {
        return this.amount;
    }

    public void setAmount(double newAmount) {
        this.amount = newAmount;
    }


    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
