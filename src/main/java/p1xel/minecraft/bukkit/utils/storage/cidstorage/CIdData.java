package p1xel.minecraft.bukkit.utils.storage.cidstorage;


import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import p1xel.minecraft.bukkit.MyCompany;

import java.io.File;
import java.io.IOException;

public class CIdData {

    private static File file;
    private static FileConfiguration yaml;

    public static void init() {
        File cidFile = new File(MyCompany.getInstance().getDataFolder(), "cids.yml");
        if (!cidFile.exists()) {
            MyCompany.getInstance().saveResource("cids.yml", false);
        }
        file = cidFile;
        yaml = YamlConfiguration.loadConfiguration(cidFile);

    }

    public static void upload(File f, FileConfiguration y) {
        file = f;
        yaml = y;
    }

    public static FileConfiguration get() {
        return yaml;
    }

    public static void set(String path, Object value) {
        yaml.set(path, value);
        try {
            yaml.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getCID() {
        return yaml.getInt("cid");
    }

    public static void setCID(int number) {
        set("cid", number);
    }

    public static int getAndUpdateCID() {
        int cid = getCID();
        set("cid", cid+1);
        return cid;
    }

}
