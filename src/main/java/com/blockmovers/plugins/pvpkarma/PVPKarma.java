package com.blockmovers.plugins.pvpkarma;

import java.util.logging.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.tag.PlayerReceiveNameTagEvent;

public class PVPKarma extends JavaPlugin implements Listener {
    
    static final Logger log = Logger.getLogger("Minecraft"); //set up our logger
    
    public void onEnable() {
        PluginDescriptionFile pdffile = this.getDescription();
        PluginManager pm = this.getServer().getPluginManager(); //the plugin object which allows us to add listeners later on

        pm.registerEvents(this, this);

        log.info(pdffile.getName() + " version " + pdffile.getVersion() + " is enabled.");
    }

    public void onDisable() {
        PluginDescriptionFile pdffile = this.getDescription();

        log.info(pdffile.getName() + " version " + pdffile.getVersion() + " is disabled.");
    }

    @EventHandler
    public void onNameTag(PlayerReceiveNameTagEvent event) {
        //event.getPlayer() The player who will see the tag
        //event.getNamedPlayer() The player who will have the modified tag
    }
}

