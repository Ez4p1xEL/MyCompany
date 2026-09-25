package p1xel.minecraft.bukkit.object.focus;

import p1xel.minecraft.bukkit.object.Company;

/**
 * Represents a company focus and the expertises.
 */

public class CompanyFocus {

    private final Focus focus;
    private final Company company;

    public CompanyFocus(Focus focus, Company company) {
        this.focus = focus;
        this.company = company;
    }

    public Focus getFocus() {
        return focus;
    }

    public Company getCompany() {
        return company;
    }

}
