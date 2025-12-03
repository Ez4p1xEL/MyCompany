package p1xel.minecraft.bukkit.utils.storage;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import p1xel.minecraft.bukkit.Company;
import p1xel.minecraft.bukkit.managers.areas.CompanyArea;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MysqlCompanyData extends CompanyData{
    @Override
    public void init() {

    }

    @Override
    public Company getCompany(UUID uniqueId) {
        return null;
    }

    @Override
    public List<UUID> getCompaniesUUID() {
        return null;
    }

    @Override
    public List<String> getCompaniesName() {
        return null;
    }

    @Override
    public void set(UUID uniqueId, String type, String key, Object value) {

    }

    @Override
    public void createCompany(String companyName, UUID playerUniqueId) {

    }

    @Override
    public Object get(UUID uniqueId, String type, String path) {
        return null;
    }

    @Override
    public String getName(UUID uniqueId) {
        return "";
    }

    @Override
    public int getId(UUID uniqueId) {
        return 0;
    }

    @Override
    public UUID getFounder(UUID uniqueId) {
        return null;
    }

    @Override
    public String getFounderName(UUID uniqueId) {
        return "";
    }

    @Override
    public UUID getEmployer(UUID uniqueId) {
        return null;
    }

    @Override
    public List<UUID> getEmployeeList(UUID uniqueId, String position) {
        return null;
    }

    @Override
    public void dismissEmployee(UUID companyUniqueId, UUID employeeUniqueId) {

    }

    @Override
    public void employPlayer(UUID companyUniqueId, UUID playerUniqueId, String position) {

    }

    @Override
    public void disbandCompany(UUID uniqueId) {

    }

    @Override
    public String getFoundDate(UUID uniqueId) {
        return "";
    }

    @Override
    public int getMemberAmount(UUID uniqueId) {
        return 0;
    }

    @Override
    @Nullable
    public UUID getUUIDFromId(int cid) {
        return null;
    }

    @Override
    public List<String> getPositions(UUID uniqueId) {
        return null;
    }

    @Override
    public List<UUID> getAllCompanies() {
        return null;
    }

    @Override
    public double getCash(UUID uniqueId) {
        return 0;
    }

    @Override
    public void setCash(UUID uniqueId, double amount) {

    }

    @Override
    public double getDailyIncome(UUID uniqueId) {
        return 0;
    }

    @Override
    public double getTotalIncome(UUID uniqueId) {
        return 0;
    }

    @Override
    public void giveMoney(UUID uniqueId, double amount) {

    }

    @Override
    public void takeMoney(UUID uniqueId, double amount) {

    }

    @Override
    public double getSalary(UUID uniqueId, String position) {
        return 0;
    }

    @Override
    public void setSalary(UUID uniqueId, String position, double amount) {

    }

    @Override
    public void resetDailyIncome(UUID uniqueId) {

    }

    @Override
    public UUID createShop(UUID uniqueId, Location location, double price, String creatorName) {
        return null;
    }

    @Override
    public List<Shop> getShops(UUID uniqueId) {
        return null;
    }

    @Override
    public List<UUID> getShopsUUID(UUID uniqueId) {
        return null;
    }

    @Override
    public ItemStack getItem(UUID companyUniqueId, UUID shopUniqueId) {
        return null;
    }

    @Override
    public @org.jetbrains.annotations.Nullable Location getLocation(UUID uniqueId) {
        return null;
    }

    @Override
    public void setLocation(UUID uniqueId, Location location) {

    }

    @Override
    public int getLocationPos(UUID uniqueId, String area, String type) {
        return 0;
    }

    @Override
    public void createArea(UUID uniqueId, CompanyArea companyArea, OfflinePlayer creator, Location firstBlock, Location secondBlock) {

    }

    @Override
    public Set<String> getAreas(UUID uniqueId) {
        return Set.of();
    }

    @Override
    public void setAccessibleCompanies(UUID uniqueId, String area, List<UUID> companyList) {

    }

    @Override
    public List<UUID> getAccessibleCompanies(UUID uniqueId, String area) {
        return List.of();
    }

    @Override
    public void deleteArea(UUID uniqueId, CompanyArea companyArea) {

    }

    @Override
    public void setAreaLocation(UUID uniqueId, String area, Location location) {

    }

    @Override
    public Location getAreaLocation(UUID uniqueId, String area) {
        return null;
    }

    @Override
    public HashMap<UUID, List<String>> getAreasRented(UUID uniqueId) {
        return null;
    }
}
