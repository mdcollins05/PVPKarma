/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blockmovers.plugins.pvpkarma;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.kitteh.tag.PlayerReceiveNameTagEvent;

/**
 *
 * @author MattC
 */
public class Listeners implements Listener {

    PVPKarma plugin = null;

    public Listeners(PVPKarma plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNameTag(PlayerReceiveNameTagEvent event) {
        //event.getPlayer() The player who will see the tag
        //event.getNamedPlayer() The player who will have the modified tag
        event.setTag(this.plugin.getKarmaColor(event.getNamedPlayer().getName()) + event.getNamedPlayer().getName());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player attacker = null;
        Arrow arrow = null;
        Entity damageSource = event.getDamager();
        if (damageSource instanceof Player) {
            attacker = (Player) damageSource;
        } else if (damageSource instanceof Arrow) {
            arrow = (Arrow) damageSource;
            if (arrow.getShooter() instanceof Player) {
                attacker = (Player) arrow.getShooter();
            }
        } else if (damageSource instanceof ThrownPotion) {
            ThrownPotion potion = (ThrownPotion) damageSource;
            if (potion.getShooter() instanceof Player) {
                attacker = (Player) potion.getShooter();
            }
        }

        if (event.getEntity() instanceof Player && attacker instanceof Player) {
            if (attacker.getName().equalsIgnoreCase(((Player) event.getEntity()).getName())) {
                return;
            }
            if (!this.plugin.checkPVP(attacker, (Player) event.getEntity())) {
                event.setCancelled(true);
            }
        }

        if (attacker == null) {
            return;
        }

        double karma = this.plugin.karma.getKarma(attacker.getName());
        Integer oldDamage = event.getDamage();

        if (this.plugin.isGood(attacker.getName())) {
            if (this.plugin.chance((int) karma, 1000)) {
                Float randomMultiplier = (this.plugin.random(40)) / 100F;

                Integer newDamage = Math.round((oldDamage * randomMultiplier) + oldDamage);
                if (newDamage == oldDamage) {
                    newDamage++;
                }
                event.setDamage(newDamage);
                //attacker.sendMessage("Attack did " + newDamage + " damage!(rounding up (" + oldDamage + " * " + randomMultiplier + "))");
            } else {
                //attacker.sendMessage("Attack did " + oldDamage + " damage!");
            }
        } else if (this.plugin.isBad(attacker.getName())) {
            double inverse = 1000 - (karma + 1000);
            if (this.plugin.chance((int) inverse, 1000)) {
                event.setDamage(0); //possible the attack "misses"
                //attacker.sendMessage("Attack did 0 damage!(" + oldDamage + ")");
            } else {
                //attacker.sendMessage("Attack did " + oldDamage + " damage!");
            }
        } else {
            //attacker.sendMessage("Attack did " + oldDamage + " damage!");
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() == null) {
            return;
        }
        Player killer = event.getEntity().getKiller();
        this.plugin.updateKarma(killer, this.plugin.getNewKarmaPVPMath(killer.getName(), event.getEntity().getName()));
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) {
            return;
        }
        Player p = event.getEntity().getKiller();
        double karma = this.plugin.karma.getKarma(p.getName()) - this.plugin.getMobKarma(event.getEntity());
        this.plugin.updateKarma(p.getName(), karma);
    }
}
