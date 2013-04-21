package com.blockmovers.plugins.pvpkarma;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.tag.TagAPI;

public class PVPKarma extends JavaPlugin implements Listener {

    static final Logger log = Logger.getLogger("Minecraft"); //set up our logger
    private Random randomGenerator = new Random();
    public Karma karma = new Karma(this);
    public Map<String, Double> mobKarma = new HashMap();
    public Integer karma_bad = -20;
    public Integer karma_good = 20;

    public void onEnable() {
        PluginDescriptionFile pdffile = this.getDescription();
        PluginManager pm = this.getServer().getPluginManager(); //the plugin object which allows us to add listeners later on

        pm.registerEvents(new Listeners(this), this);

        this.karma.load();
        //this.setMobKarmaList();

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
                } else {
                    return false;
                }
            } else {
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
                if (this.karma.togglePVP(target.getName())) {
                    cs.sendMessage(ChatColor.GREEN + "PVP disabled!");
                } else {
                    cs.sendMessage(ChatColor.GREEN + "PVP enabled!");
                }
            }
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("karma")) {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("version")) {
                    PluginDescriptionFile pdf = this.getDescription();
                    cs.sendMessage(pdf.getName() + " " + pdf.getVersion() + " by MDCollins05");
                    return true;
                } else if (args[0].equalsIgnoreCase("set")) {
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
                    this.updateKarma(name, Integer.parseInt(args[2]));
                    cs.sendMessage(ChatColor.GREEN + "Karma for " + name + " is now " + args[2] + "!");
                    return true;
                } else {
                    name = this.getPlayerName(args[0], true);

                    if (name == null) {

                        cs.sendMessage(ChatColor.RED + "Couldn't find player " + args[0] + "!");
                        return false;
                    }
                    cs.sendMessage(ChatColor.GREEN + "Karma for " + name + " is " + ((int) this.karma.getKarma(name)) + "!");
                }
            } else {
                cs.sendMessage(ChatColor.GREEN + "Your karma rating is " + ((int) this.karma.getKarma(cs.getName())) + "!");
                return true;
            }
            return true;
        }
        return false;
    }

    public double getNewKarmaPVPMath(String killer, String killed) {
        double newK = this.getNewKarmaMath(this.karma.getKarma(killer), this.karma.getKarma(killed));
        //if (newK >= 0 && newK <= 1) {
        newK = newK - 5;
        //}
        return newK;
    }

    public double getNewKarmaMath(double killer, double killed) {
        return (((killer - killed) / 10) + killer);
    }

    public String getNewKarmaMsg(double oldK, double newK) {
        if (oldK > newK) {
            return (ChatColor.RED + "You probably shouldn't have done that.");
        } else if (oldK < newK) {
            return (ChatColor.GREEN + "You gained karma from that!");
        }
        return null;
    }

    public boolean hasKarmaChanged(Double oK, Double nK) {
        if (this.getKarmaColor(oK).equals(this.getKarmaColor(nK))) {
            return true;
        }
        return false;
    }

    public String getKarmaColor(String p) {
        return this.getKarmaColor(this.karma.getKarma(p));
    }

    public String getKarmaColor(double k) {
        if (k >= this.karma_good) {
            return ChatColor.GREEN.toString();
        }
        if (k <= this.karma_bad) {
            return ChatColor.RED.toString();
        }
        return "";
    }

    public Boolean isGood(String p) {
        double k = this.karma.getKarma(p);
        if (k >= this.karma_good) {
            return true;
        }
        return false;
    }

    public Boolean isBad(String p) {
        double k = this.karma.getKarma(p);
        if (k <= this.karma_bad) {
            return true;
        }
        return false;
    }

    public void updateKarma(String p, double k) {
        Double oK = this.karma.getKarma(p);
        this.karma.setKarma(p, k);
        if (this.getServer().getPlayer(p) != null) {
            if (this.hasKarmaChanged(oK, k)) {
                TagAPI.refreshPlayer(this.getServer().getPlayer(p));
            }
        }
    }

    public void updateKarma(Player p, double k) {
        this.updateKarma(p.getName(), k);
    }

    public boolean checkPVP(Player attacker, Player attacked) {
        String attackerName = attacker.getName();
        String attackedName = attacked.getName();
        if (attacker.getGameMode().equals(GameMode.CREATIVE)) {
            return false;
        }

        if (this.karma.getPVP(attackedName)) {
            attacker.sendMessage(ChatColor.RED + attackedName + " has toggled PVP off!");
            return false;
        }

        if (this.karma.getPVP(attackerName)) {
            attacker.sendMessage(ChatColor.RED + "You have disabled PVP! Use /pvp to change your mode!");
            return false;
        }
        return true;
    }

    public String getPlayerName(String s, Boolean offline) {
        if (this.getServer().getPlayer(s) != null) {
            return this.getServer().getPlayer(s).getName();
        }
        if (offline) {
            if (this.getServer().getOfflinePlayer(s).hasPlayedBefore()) {
                return s;
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

//    private void setMobKarmaList() {
//        this.mobKarma.put(EntityType.BLAZE.getName(), -.1);
//        this.mobKarma.put(EntityType.CAVE_SPIDER.getName(), -.1);
//        this.mobKarma.put(EntityType.CHICKEN.getName(), .1);
//        this.mobKarma.put(EntityType.COW.getName(), .1);
//        this.mobKarma.put(EntityType.CREEPER.getName(), -.1);
//        this.mobKarma.put(EntityType.ENDERMAN.getName(), -.1);
//        this.mobKarma.put(EntityType.GHAST.getName(), -.1);
//        this.mobKarma.put(EntityType.GIANT.getName(), -.1);
//        this.mobKarma.put(EntityType.IRON_GOLEM.getName(), .1);
//        this.mobKarma.put(EntityType.MAGMA_CUBE.getName(), -.1);
//        this.mobKarma.put(EntityType.MUSHROOM_COW.getName(), .1);
//        this.mobKarma.put(EntityType.OCELOT.getName(), .1);
//        this.mobKarma.put(EntityType.PIG.getName(), .1);
//        this.mobKarma.put(EntityType.PIG_ZOMBIE.getName(), -.1);
//        this.mobKarma.put(EntityType.SHEEP.getName(), .1);
//        this.mobKarma.put(EntityType.SILVERFISH.getName(), -.1);
//        this.mobKarma.put(EntityType.SKELETON.getName(), -.1);
//        this.mobKarma.put(EntityType.SLIME.getName(), -.1);
//        this.mobKarma.put(EntityType.SNOWMAN.getName(), .1);
//        this.mobKarma.put(EntityType.SPIDER.getName(), -.1);
//        this.mobKarma.put(EntityType.VILLAGER.getName(), .1);
//        this.mobKarma.put(EntityType.WITCH.getName(), -.1);
//        this.mobKarma.put(EntityType.WITHER.getName(), -.1);
//        this.mobKarma.put(EntityType.ZOMBIE.getName(), -.1);
//    }
    public double getMobKarma(LivingEntity entity) {
        if (entity instanceof Animals) {
            return .1;
        } else if (entity instanceof Monster) {
            return -.1;
        }
        return 0;
    }

    public boolean chance(Integer percent, Integer ceiling) {
        Integer randomInt = this.random(ceiling);
        if (randomInt < percent) {
            return true;
        }
        return false;
    }

    public Integer random(Integer ceil) {
        Integer randomInt = this.randomGenerator.nextInt(ceil * 1000); //moar random?
        Integer value = randomInt / 1000; //I think so, so now we fix that and round
        return value;
    }
}
