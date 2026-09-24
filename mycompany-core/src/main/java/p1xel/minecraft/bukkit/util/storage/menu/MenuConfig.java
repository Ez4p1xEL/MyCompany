package p1xel.minecraft.bukkit.util.storage.menu;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import p1xel.minecraft.bukkit.MyCompany;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MenuConfig {

    public static void initialization() {

        File file = new File(MyCompany.getInstance().getDataFolder(), "menus.yml");
        YamlConfiguration exist_file = YamlConfiguration.loadConfiguration(file);

        if (!file.exists()) {
            MyCompany.getInstance().saveResource("menus.yml", false);
        } else {
            InputStreamReader newFile = new InputStreamReader(MyCompany.getInstance().getResource("menus.yml"), StandardCharsets.UTF_8);
            YamlConfiguration latest_file = YamlConfiguration.loadConfiguration(newFile);

            // Gets all the keys inside the internal file and iterates through all of it's key pairs
            for (String string : latest_file.getKeys(true)) {
                // Checks if the external file contains the key already.
                if (!exist_file.contains(string)) {
                    // If it doesn't contain the key, we set the key based off what was found inside the plugin jar
                    exist_file.set(string, latest_file.get(string));
                }
            }

            try {
                exist_file.save(file);
            } catch (IOException io) {
                io.printStackTrace();
            }
        }

        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        GLOBAL_BACK_TO_MAIN = yaml.getString("global.back-to-main");
        GLOBAL_NEXT_PAGE = yaml.getString("global.next-page");
        GLOBAL_PREVIOUS_PAGE = yaml.getString("global.previous-page");
        GLOBAL_EMPTY_SLOT = yaml.getString("global.empty-slot");

    }

    public static String GLOBAL_BACK_TO_MAIN;
    public static String GLOBAL_NEXT_PAGE;
    public static String GLOBAL_PREVIOUS_PAGE;
    public static String GLOBAL_EMPTY_SLOT;

}
