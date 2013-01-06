package com.blockmovers.plugins.pvpkarma;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.tag.TagAPI;

public class PVPKarma extends JavaPlugin implements Listener {

    static final Logger log = Logger.getLogger("Minecraft"); //set up our logger
    public Karma karma = new Karma(this);
    public Map<String, Integer> mobKarma = new HashMap();

    public void onEnable() {
        PluginDescriptionFile pdffile = this.getDescription();
        PluginManager pm = this.getServer().getPluginManager(); //the plugin object which allows us to add listeners later on

        pm.registerEvents(new Listeners(this), this);

        this.karma.load();

        log.info(pdffile.getName() + " version " + pdffile.getVersion() + " is enabled.");
    }

    public void onDisable() {
        PluginDescriptionFile pdffile = this.getDescription();

        log.info(pdffile.getName() + " version " + pdffile.getVersion() + " is disabled.");
    }

    public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
        Player target = null;
        String name = null;
        if (cmd.getName().equalsIgnoreCase("pvp")) {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("version")) {
                    PluginDescriptionFile pdf = this.getDescription();
                    cs.sendMessage(pdf.getName() + " " + pdf.getVersion() + " by MDCollins05");
                    return true;
                } else if (args[0].equalsIgnoreCase("toggle") || args[0].equalsIgnoreCase("t")) {
                    if (cs instanceof Player) {
                        Player s = (Player) cs;
                        if (s.hasPermission("pvpkarma.pvp.toggle") || this.karma.getPVP(s.getName())) {
                            target = (Player) s;
                        } else {
                            s.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                            return false;
                        }
                    } else {
                        cs.sendMessage(ChatColor.RED + "Console cannot run that command.");
                        return false;
                    }
                } else {
                    return false;
                }
                if (this.karma.togglePVP(target.getName())) {
                    cs.sendMessage(ChatColor.GREEN + "PVP disabled!");
                } else {
                    cs.sendMessage(ChatColor.GREEN + "PVP enabled!");
                }
            } else {
                return false;
            }
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("karma")) {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("version")) {
                    PluginDescriptionFile pdf = this.getDescription();
                    cs.sendMessage(pdf.getName() + " " + pdf.getVersion() + " by MDCollins05");
                    return true;
                } else if (args[0].equalsIgnoreCase("edit")) {
                    if (cs instanceof Player) {
                        if (!cs.hasPermission("pvpkarma.karma.change")) {
                            cs.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                            return false;
                        }
                    }
                    if (args.length != 3) {
                        cs.sendMessage(ChatColor.RED + "You must specify both a player and new karma level!");
                        return false;
                    }
                    name = this.getPlayerName(args[1], true);
                    if (name == null) {
                        cs.sendMessage(ChatColor.RED + "Couldn't find player " + args[1] + "!");
                        return false;
                    }
                    if (!this.isInteger(args[2])) {
                        cs.sendMessage(ChatColor.RED + "Karma must be a whole number!");
                        return false;
                    }
                    this.updateKarma(target, Integer.parseInt(args[2]));
                    cs.sendMessage(ChatColor.GREEN + "Karma for " + name + " is now " + args[2] + "!");
                    return true;
                } else {
                    name = this.getPlayerName(args[0], true);
                    if (target == null) {
                        cs.sendMessage(ChatColor.RED + "Couldn't find player " + args[0] + "!");
                        return false;
                    }
                    cs.sendMessage(ChatColor.GREEN + "Karma for " + name + " is " + this.karma.getKarma(name) + "!");
                }
            } else {
                cs.sendMessage(ChatColor.GREEN + "Your karma rating is " + this.karma.getKarma(cs.getName()) + "!");
                return true;
            }
            return true;
        }
        return false;
    }

    public int getNewKarmaPVPMath(String killer, String killed) {
        int newK = this.getNewKarmaMath(this.karma.getKarma(killer), this.karma.getKarma(killed));
        //if (newK >= 0 && newK <= 1) {
        newK--;
        //}
        return newK;
    }

    public int getNewKarmaMath(int killer, int killed) {
        return (((killer - killed) / 10) + killer);
    }

    public String getNewKarmaMsg(int oldK, int newK) {
        if (oldK > newK) {
            return (ChatColor.RED + "You probably shouldn't have done that.");
        } else if (oldK < newK) {
            return (ChatColor.GREEN + "You gained karma from that!");
        }
        return null;
    }

    public String getKarmaColor(String p) {
        return this.getKarmaColor(this.karma.getKarma(p));
    }

    public String getKarmaColor(int k) {
        if (k >= 20) {
            return ChatColor.GREEN.toString();
        }
        if (k <= -20) {
            return ChatColor.RED.toString();
        }
        return "";
    }

    public void updateKarma(String p, int k) {
        if (this.getServer().getPlayer(p) != null) {
            TagAPI.refreshPlayer(this.getServer().getPlayer(p));
        } else {
            this.karma.setKarma(p, k);
        }
    }

    public void updateKarma(Player p, int k) {
        this.karma.setKarma(p.getName(), k);
        TagAPI.refreshPlayer(p);
    }

    public boolean checkPVP(Player attacker, Player attacked) {
        String attackerName = attacker.getName();
        String attackedName = attacked.getName();
        if (!attacker.getGameMode().equals(GameMode.SURVIVAL)) {
            return false;
        }

        if (this.karma.getPVP(attackedName)) {
            attacker.sendMessage(ChatColor.RED + attackedName + " has toggled PVP off!");
            return false;
        }

        if (this.karma.getPVP(attackerName)) {
            attacker.sendMessage(ChatColor.RED + "You have disabled PVP! Use /pvp toggle to change your mode!");
            return false;
        }
        return true;
    }

    public String getPlayerName(String s, Boolean offline) {
        if (this.getServer().getPlayer(s) != null) {
            return this.getServer().getPlayer(s).getName();
        }
        if (offline) {
            if (this.getServer().getOfflinePlayer(s) != null) {
                return this.getServer().getOfflinePlayer(s).getName();
            }
        }
        return null;
    }

    public boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
//    public int getMobKarma(String mob) {
//        
//        }
//    }
}
