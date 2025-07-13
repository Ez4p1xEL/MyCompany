package p1xel.minecraft.bukkit.utils.storage;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.utils.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class YamlUserData extends UserData{

    private HashMap<UUID, File> files = new HashMap<>();
    private HashMap<UUID, FileConfiguration> yamls = new HashMap<>();

    @Override
    public void init() {
        File folder = new File(MyCompany.getInstance().getDataFolder(), "/users");
        if (!folder.exists()) {
            folder.mkdirs();
            Logger.debug(Level.INFO, "users folder has been created for the first time.");
        }
//        files.clear();
//        yamls.clear();
//        File[] users = folder.listFiles();
//        if (users != null) {
//            for (File file : users) {
//                UUID uniqueId = UUID.fromString(file.getName().replaceFirst("[.][^.]+$", ""));
//                files.put(uniqueId,file);
//                yamls.put(uniqueId, YamlConfiguration.loadConfiguration(file));
//
//            }
//        }
    }

    // Also create Cache
    @Override
    public void createUser(UUID uniqueId) {
        File file = new File(MyCompany.getInstance().getDataFolder() + "/users", uniqueId + ".yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            OfflinePlayer player = Bukkit.getOfflinePlayer(uniqueId);
            yaml.set(uniqueId + ".name", player.getName());
            try {
                yaml.save(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            files.put(uniqueId, file);
            yamls.put(uniqueId, yaml);

            Logger.log(Level.INFO, "User of " + player.getName() + " has been created for the first time");
            return;

        }

        if (files.get(uniqueId) == null) {
            files.put(uniqueId, file);
            yamls.put(uniqueId, YamlConfiguration.loadConfiguration(file));
        } else {
            files.replace(uniqueId, file);
            yamls.replace(uniqueId, YamlConfiguration.loadConfiguration(file));
        }

    }

    @Override
    public Object get(UUID uniqueId, String path) {
        FileConfiguration yaml = yamls.get(uniqueId);
        return yaml.get(uniqueId + "." + path);
    }

    @Override
    public void set(UUID uniqueId, String path, Object value) {
        File file = files.get(uniqueId);
        FileConfiguration yaml = yamls.get(uniqueId);
        yaml.set(uniqueId + "." + path, value);
        try {
            yaml.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //yamls.replace(uniqueId,yaml);
    }

    @Override
    public String getName(UUID uniqueId) {
        return (String) get(uniqueId, "name");
    }

    @Override
    public boolean isUserExist(UUID uniqueId) {
        File file = new File(MyCompany.getInstance().getDataFolder() +"/users", uniqueId + ".yml");
        return file.exists();
    }

    @Override
    public void setCompany(UUID playerUniqueId, UUID companyUniqueId) {
        String uuid;
        if (companyUniqueId != null ) {
            uuid = String.valueOf(companyUniqueId);
        } else {
            uuid = null;
        }
        set(playerUniqueId, "company.uuid", uuid);
    }

    @Override
    @Nullable
    public UUID getCompanyUUID(UUID uniqueId) {
        try {
            return UUID.fromString((String) get(uniqueId, "company.uuid"));
        } catch (NullPointerException exception){
            return null;
        }
    }

    @Override
    public void setPosition(UUID uniqueId, String position) {
        set(uniqueId, "company.position", position);
    }

    @Override
    public String getPosition(UUID uniqueId) {
        return (String) get(uniqueId, "company.position");
    }

}
