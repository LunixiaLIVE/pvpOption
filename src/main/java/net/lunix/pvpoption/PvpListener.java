package net.lunix.pvpoption;

import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PvpListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player defender)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (attacker.equals(defender)) return;
        if (!PvpManager.shouldAllowDamage(attacker, defender))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        PvpManager.cooldownExpiry.remove(uuid);
        PvpManager.warmupExpiry.remove(uuid);
        PvpManager.lastActivityTime.remove(uuid);
    }
}
