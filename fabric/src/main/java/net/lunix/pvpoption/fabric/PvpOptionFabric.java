package net.lunix.pvpoption.fabric;

import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.lunix.pvpoption.PvpOptionCommon;
import net.lunix.pvpoption.PlayerDataStore;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class PvpOptionFabric implements ModInitializer {

    // Legacy attachment — only used to migrate existing player NBT data to PlayerDataStore.
    public static final AttachmentType<Boolean> PVP_FLAGGED = AttachmentRegistry.create(
        Identifier.fromNamespaceAndPath(PvpOptionCommon.MOD_ID, "pvp_flagged"),
        builder -> builder.persistent(Codec.BOOL).initializer(() -> false)
    );

    @Override
    public void onInitialize() {
        PvpOptionCommon.expRepairLoaded = FabricLoader.getInstance().isModLoaded("exprepair");
        PvpOptionCommon.configFolder = FabricLoader.getInstance().getConfigDir();
        PvpOptionCommon.gameFolder = FabricLoader.getInstance().getGameDir();
        PvpOptionCommon.init();

        ServerLifecycleEvents.SERVER_STARTED.register(PvpOptionCommon::onServerStarted);
        ServerTickEvents.END_SERVER_TICK.register(PvpOptionCommon::onServerTick);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            PvpOptionCommon.registerCommands(dispatcher));

        // Damage cancellation — Fabric-specific event
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayer defender)) return true;
            Entity responsible = source.getEntity();
            if (!(responsible instanceof ServerPlayer attacker)) return true;
            if (attacker == defender) return true;
            return PvpOptionCommon.shouldAllowDamage(attacker, defender);
        });

        // Legacy NBT → PlayerDataStore migration on join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            boolean legacyFlagged = player.getAttachedOrElse(PVP_FLAGGED, false);
            if (legacyFlagged && !PlayerDataStore.isPvpFlagged(player.getUUID())) {
                PvpOptionCommon.LOGGER.info("Migrating legacy pvpFlagged for player {}", player.getName().getString());
                PlayerDataStore.setPvpFlagged(player.getUUID(), true);
            }
            player.setAttached(PVP_FLAGGED, null);
        });

        // Cleanup runtime maps on disconnect
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            PvpOptionCommon.onPlayerQuit(handler.getPlayer()));
    }
}
