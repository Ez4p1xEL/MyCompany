package p1xel.minecraft.bukkit.util.storage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.util.ColorUtil;
import p1xel.minecraft.bukkit.util.Config;
import p1xel.minecraft.bukkit.util.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class Locale {

    public static File file;
    public static FileConfiguration yaml;

    private static String prefix;
    private static String version;

    public static void createLocaleFile() {

        List<String> lang = Arrays.asList("en","zh_CN","zh_TW");
        for (String l : lang) {
            File file = new File(MyCompany.getInstance().getDataFolder() + "/lang", l + ".yml");
            YamlConfiguration exist_file = YamlConfiguration.loadConfiguration(file);
            if (!file.exists()) {
                MyCompany.getInstance().saveResource("lang/" + l + ".yml", false);
            } else {
                InputStreamReader newFile = new InputStreamReader(MyCompany.getInstance().getResource("lang/"+ l +".yml"), StandardCharsets.UTF_8);
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
        }

        upload(new File(MyCompany.getInstance().getDataFolder() + "/lang", Config.getLanguage() + ".yml"));
    }

    public static void upload(File locale) {
        file = locale;
        yaml = YamlConfiguration.loadConfiguration(locale);
        prefix = yaml.getString("prefix");
        version = yaml.getString("version");
    }

    public static void set(String path, Object value) {
        yaml.set(path,value);
        try {
            yaml.save(file);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }

    public static String getMessage(String path) {
        try {
            return ColorUtil.translateHexColorCodes(yaml.getString(path).replaceAll("%prefix%", prefix).replaceAll("%version%", version));
        } catch (NullPointerException event){
            Logger.debug(Level.WARNING, path + " is not found in your lang file!");
            Logger.debug(Level.WARNING, "Your language file is not updated to the latest. Please delete it and let it to be re-generated.");
            return "Please update the language file.";
        }
    }

    public static List<String> getMessageList(String path) {
        List<String> list = yaml.getStringList(path);
        for (int i = 0; i < list.size(); i++) {
            list.set(i, translate(list.get(i)));
        }
        return list;
    }

    public static String getCmdMessage(String path) {
        return ColorUtil.translateHexColorCodes(yaml.getString(path).replaceAll("%prefix%", prefix).replaceAll("%version%", version));
    }

    public static String translate(String message) {
        return ColorUtil.translateHexColorCodes(message.replaceAll("%prefix%", prefix).replaceAll("%version%", version));
    }

    public static List<String> translateList(List<String> messages) {
        messages.replaceAll(Locale::translate);
        return messages;
    }

    public static String getYesOrNo(boolean bool) {
        return getMessage(Boolean.toString(bool).toLowerCase());
    }

    public static String deserialize(Component component) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(component);
    }


}
