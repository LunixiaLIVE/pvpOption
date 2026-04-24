package net.lunix.pvpoption.neoforge;

import net.lunix.pvpoption.PvpOptionCommon;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@Mod(PvpOptionCommon.MOD_ID)
public class PvpOptionNeoForge {

    public PvpOptionNeoForge() {
        PvpOptionCommon.init();
        NeoForge.EVENT_BUS.register(PvpOptionNeoForge.class);
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer defender)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) return;
        if (attacker == defender) return;
        if (!PvpOptionCommon.shouldAllowDamage(attacker, defender)) {
            event.setCanceled(true);
        }
    }
}
