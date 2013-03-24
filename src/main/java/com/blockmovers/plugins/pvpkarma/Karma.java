package com.blockmovers.plugins.pvpkarma;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Karma {

    PVPKarma plugin = null;
    FileConfiguration karma = null;
    private File karmaFile = null;
    private Map<String, Boolean> PVPToggle = new HashMap(); 

    public Karma(PVPKarma plugin) {
        this.plugin = plugin;
    }
    
    public void reload() {
        if (karmaFile == null) {
            karmaFile = new File(plugin.getDataFolder(), "karma.yml");
        }
        karma = YamlConfiguration.loadConfiguration(karmaFile);

        // Look for defaults in the jar
        InputStream defConfigStream = plugin.getResource("karma.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            karma.setDefaults(defConfig);
        }
    }

    public FileConfiguration load() {
        if (karma == null) {
            reload();
        }
        return karma;
    }

    public void save() {
        if (karma == null || karmaFile == null) {
            return;
        }
        try {
            karma.save(karmaFile);
        } catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save the file: " + karmaFile, ex);
        }
    }
    
    public double getKarma(String p) {
        return this.karma.getDouble("karma." + p, 0);
    }
    
    public void setKarma(String p, double k) {
        if (k > 1000) {
            k = 1000;
        }
        if (k == 0) {
            this.karma.set("karma." + p, null);
        } else {
            this.karma.set("karma." + p, k);
        }
        this.save();
    }
    
    public boolean getPVP(String p) {
        if (this.PVPToggle.containsKey(p)) {
            return this.PVPToggle.get(p);
        }
        return false;
    }
    
    public boolean togglePVP(String p) {
        if (this.getPVP(p)) {
            this.PVPToggle.remove(p);
            this.karma.set("PVPToggle." + p, null);
            this.save();
            return false;
        } else {
            this.PVPToggle.put(p, true);
            this.karma.set("PVPToggle." + p, true);
            this.save();
            return true;
        }
    }
}
