package p1xel.minecraft.bukkit;

import p1xel.minecraft.bukkit.managers.CompanyManager;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Company {

    private final UUID companyUniqueId;
    private final CompanyManager companyManager;

    private String name;
    private UUID employerUniqueId;
    private final HashMap<String, List<UUID>> employeeList = new HashMap<>();
    private List<String> positions;

    public Company(UUID companyUniqueId) {
        this.companyUniqueId = companyUniqueId;
        this.companyManager = MyCompany.getCacheManager().getCompanyManager();

        this.name = companyManager.getName(companyUniqueId);
        this.employerUniqueId = companyManager.getEmployer(companyUniqueId);
        this.positions = companyManager.getPositions(companyUniqueId);
        for (String position : positions) {
            this.employeeList.put(position, companyManager.getEmployeeList(companyUniqueId, position));
        }

    }

    public UUID getUUID() {
        return companyUniqueId;
    }

    public String getName() {
        return name;
    }

    public UUID getEmployer() {
        return employerUniqueId;
    }

    public List<UUID> getEmployeeList(String position) {
        return employeeList.get(position);
    }

    public List<String> getPositions() {
        return positions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmployer(UUID employerUniqueId) {
        this.employerUniqueId = employerUniqueId;
    }

    public void setEmployeeList(String position, List<UUID> employees) {
        this.employeeList.put(position, employees);
    }

    public void setPositions(List<String> positions) {
        this.positions = positions;
    }

}
