package net.lunix.pvpoption;

import com.google.gson.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PvpManager {

    static boolean pvpEnabled        = true;
    static boolean broadcastToggle   = true;
    static int     cooldownSeconds   = 30;
    static int     warmupSeconds     = 5;
    static int     autoUnflagMinutes = 0;

    static final Map<UUID, Long> cooldownExpiry   = new ConcurrentHashMap<>();
    static final Map<UUID, Long> warmupExpiry     = new ConcurrentHashMap<>();
    static final Map<UUID, Long> lastActivityTime = new ConcurrentHashMap<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static PvpOptionPaper plugin;

    static void init(PvpOptionPaper p) {
        plugin = p;
        PvpPlayerData.init(p.getDataFolder().toPath(), p.getLogger());
        loadConfig();
        PvpPlayerData.load();
        applyServerPvp(pvpEnabled);

        new BukkitRunnable() {
            @Override public void run() {
                long now = System.currentTimeMillis();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();

                    Long warmup = warmupExpiry.get(uuid);
                    if (warmup != null && now >= warmup) {
                        warmupExpiry.remove(uuid);
                        activateFlag(player);
                        player.sendMessage("§cPvP ENABLED. You can now deal and take damage from other flagged players.");
                    }

                    updateActionBar(player);

                    if (autoUnflagMinutes > 0
                            && PvpPlayerData.isPvpFlagged(uuid)
                            && getRemainingCooldown(uuid) == 0
                            && !warmupExpiry.containsKey(uuid)) {
                        Long lastActivity = lastActivityTime.get(uuid);
                        if (lastActivity != null && (now - lastActivity) > autoUnflagMinutes * 60_000L) {
                            deactivateFlag(player);
                            player.sendMessage("§aPvP automatically disabled due to inactivity.");
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    static void shutdown() {
        PvpPlayerData.save();
    }

    static boolean shouldAllowDamage(Player attacker, Player defender) {
        if (!pvpEnabled) return false;

        boolean attackerFlagged = PvpPlayerData.isPvpFlagged(attacker.getUniqueId());
        boolean defenderFlagged = PvpPlayerData.isPvpFlagged(defender.getUniqueId());

        if (!attackerFlagged || !defenderFlagged) {
            if (!attackerFlagged)
                attacker.sendMessage("§eYou are not flagged for PvP. Use /pvpoption to opt in.");
            else
                attacker.sendMessage("§eThat player is not flagged for PvP.");
            return false;
        }

        if (cooldownSeconds > 0) {
            long expiry = System.currentTimeMillis() + (cooldownSeconds * 1000L);
            cooldownExpiry.put(attacker.getUniqueId(), expiry);
            cooldownExpiry.put(defender.getUniqueId(), expiry);
        }
        long now = System.currentTimeMillis();
        lastActivityTime.put(attacker.getUniqueId(), now);
        lastActivityTime.put(defender.getUniqueId(), now);
        return true;
    }

    private static void updateActionBar(Player player) {
        UUID uuid = player.getUniqueId();
        if (warmupExpiry.containsKey(uuid)) {
            player.sendActionBar("§e⚔ Entering PvP in " + getRemainingWarmup(uuid) + "s...");
            return;
        }
        if (pvpEnabled && PvpPlayerData.isPvpFlagged(uuid)) {
            long cooldown = getRemainingCooldown(uuid);
            if (cooldown > 0)
                player.sendActionBar("§c⚔ PvP Active §7| §eCombat: " + cooldown + "s");
            else
                player.sendActionBar("§c⚔ PvP Active");
        }
    }

    static void activateFlag(Player player) {
        PvpPlayerData.setPvpFlagged(player.getUniqueId(), true);
        lastActivityTime.put(player.getUniqueId(), System.currentTimeMillis());
        if (broadcastToggle) {
            String msg = "§7[PvP] §c" + player.getName() + "§7 has entered PvP mode.";
            Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
                .forEach(p -> p.sendMessage(msg));
        }
    }

    static void deactivateFlag(Player player) {
        PvpPlayerData.setPvpFlagged(player.getUniqueId(), false);
        UUID uuid = player.getUniqueId();
        cooldownExpiry.remove(uuid);
        lastActivityTime.remove(uuid);
        if (broadcastToggle) {
            String msg = "§7[PvP] §a" + player.getName() + "§7 has left PvP mode.";
            Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
                .forEach(p -> p.sendMessage(msg));
        }
    }

    static long getRemainingCooldown(UUID uuid) {
        Long expiry = cooldownExpiry.get(uuid);
        if (expiry == null) return 0;
        return Math.max(0, (expiry - System.currentTimeMillis() + 999) / 1000);
    }

    static long getRemainingWarmup(UUID uuid) {
        Long expiry = warmupExpiry.get(uuid);
        if (expiry == null) return 0;
        return Math.max(0, (expiry - System.currentTimeMillis() + 999) / 1000);
    }

    static void applyServerPvp(boolean pvp) {
        Bukkit.getWorlds().forEach(w -> w.setPVP(pvp));
    }

    static void loadConfig() {
        Path path = configPath();
        if (!Files.exists(path)) { saveConfig(); return; }
        try (Reader r = Files.newBufferedReader(path)) {
            JsonObject obj = GSON.fromJson(r, JsonObject.class);
            if (obj != null) {
                if (obj.has("pvpEnabled"))        pvpEnabled        = obj.get("pvpEnabled").getAsBoolean();
                if (obj.has("cooldownSeconds"))   cooldownSeconds   = obj.get("cooldownSeconds").getAsInt();
                if (obj.has("warmupSeconds"))     warmupSeconds     = obj.get("warmupSeconds").getAsInt();
                if (obj.has("broadcastToggle"))   broadcastToggle   = obj.get("broadcastToggle").getAsBoolean();
                if (obj.has("autoUnflagMinutes")) autoUnflagMinutes = obj.get("autoUnflagMinutes").getAsInt();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[pvpOption] Failed to load config: " + e.getMessage());
        }
    }

    static void saveConfig() {
        try {
            Path path = configPath();
            Files.createDirectories(path.getParent());
            try (Writer w = Files.newBufferedWriter(path)) {
                JsonObject obj = new JsonObject();
                obj.addProperty("pvpEnabled",        pvpEnabled);
                obj.addProperty("cooldownSeconds",   cooldownSeconds);
                obj.addProperty("warmupSeconds",     warmupSeconds);
                obj.addProperty("broadcastToggle",   broadcastToggle);
                obj.addProperty("autoUnflagMinutes", autoUnflagMinutes);
                GSON.toJson(obj, w);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[pvpOption] Failed to save config: " + e.getMessage());
        }
    }

    private static Path configPath() {
        return plugin.getDataFolder().toPath().resolve("config.json");
    }
}
