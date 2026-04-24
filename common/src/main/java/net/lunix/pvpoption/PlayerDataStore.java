package net.lunix.pvpoption;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.architectury.platform.Platform;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataStore {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<Map<String, PlayerEntry>>() {}.getType();

    private static Map<String, PlayerEntry> data = new HashMap<>();

    public static class PlayerEntry {
        public boolean pvpFlagged = false;
        public boolean locked = false;
    }

    private static Path dataPath() {
        return Platform.getConfigFolder().resolve("pvpoption").resolve("playerdata.json");
    }

    public static void load() {
        Path path = dataPath();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                Map<String, PlayerEntry> loaded = GSON.fromJson(reader, MAP_TYPE);
                if (loaded != null) data = loaded;
            } catch (IOException e) {
                PvpOptionCommon.LOGGER.error("Failed to load player data", e);
            }
        }
    }

    public static void save() {
        try {
            Path path = dataPath();
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(data, MAP_TYPE, writer);
            }
        } catch (IOException e) {
            PvpOptionCommon.LOGGER.error("Failed to save player data", e);
        }
    }

    public static boolean isPvpFlagged(UUID uuid) {
        PlayerEntry entry = data.get(uuid.toString());
        return entry != null && entry.pvpFlagged;
    }

    public static void setPvpFlagged(UUID uuid, boolean flagged) {
        data.computeIfAbsent(uuid.toString(), k -> new PlayerEntry()).pvpFlagged = flagged;
        save();
    }

    public static boolean isLocked(UUID uuid) {
        PlayerEntry entry = data.get(uuid.toString());
        return entry != null && entry.locked;
    }

    public static void setLocked(UUID uuid, boolean locked) {
        data.computeIfAbsent(uuid.toString(), k -> new PlayerEntry()).locked = locked;
        save();
    }
}
