package net.lunix.pvpoption.fabric;

import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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
        PvpOptionCommon.init();

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
    }
}
