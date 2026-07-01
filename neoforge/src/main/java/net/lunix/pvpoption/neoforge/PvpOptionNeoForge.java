package net.lunix.pvpoption.neoforge;

import net.lunix.pvpoption.PvpOptionCommon;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(PvpOptionCommon.MOD_ID)
public class PvpOptionNeoForge {

    public PvpOptionNeoForge() {
        PvpOptionCommon.expRepairLoaded = ModList.get().isLoaded("exprepair");
        PvpOptionCommon.configFolder = FMLPaths.CONFIGDIR.get();
        PvpOptionCommon.gameFolder = FMLPaths.GAMEDIR.get();
        PvpOptionCommon.init();
        NeoForge.EVENT_BUS.register(PvpOptionNeoForge.class);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        PvpOptionCommon.onServerStarted(event.getServer());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        PvpOptionCommon.onServerTick(event.getServer());
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        PvpOptionCommon.registerCommands(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PvpOptionCommon.onPlayerQuit(player);
        }
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
