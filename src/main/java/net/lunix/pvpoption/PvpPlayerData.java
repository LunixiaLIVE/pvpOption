package net.lunix.pvpoption;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

public class PvpPlayerData {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<Map<String, PlayerEntry>>() {}.getType();

    private static Map<String, PlayerEntry> data = new HashMap<>();
    private static Logger logger;
    private static Path dataPath;

    public static class PlayerEntry {
        public boolean pvpFlagged = false;
        public boolean locked = false;
    }

    static void init(Path dataDir, Logger log) {
        logger = log;
        dataPath = dataDir.resolve("playerdata.json");
    }

    static void load() {
        if (!Files.exists(dataPath)) return;
        try (Reader r = Files.newBufferedReader(dataPath)) {
            Map<String, PlayerEntry> loaded = GSON.fromJson(r, MAP_TYPE);
            if (loaded != null) data = loaded;
        } catch (IOException e) {
            logger.warning("Failed to load pvp player data: " + e.getMessage());
        }
    }

    static void save() {
        try {
            Files.createDirectories(dataPath.getParent());
            try (Writer w = Files.newBufferedWriter(dataPath)) {
                GSON.toJson(data, MAP_TYPE, w);
            }
        } catch (IOException e) {
            logger.warning("Failed to save pvp player data: " + e.getMessage());
        }
    }

    public static boolean isPvpFlagged(UUID uuid) {
        PlayerEntry e = data.get(uuid.toString());
        return e != null && e.pvpFlagged;
    }

    public static void setPvpFlagged(UUID uuid, boolean flagged) {
        data.computeIfAbsent(uuid.toString(), k -> new PlayerEntry()).pvpFlagged = flagged;
        save();
    }

    public static boolean isLocked(UUID uuid) {
        PlayerEntry e = data.get(uuid.toString());
        return e != null && e.locked;
    }

    public static void setLocked(UUID uuid, boolean locked) {
        data.computeIfAbsent(uuid.toString(), k -> new PlayerEntry()).locked = locked;
        save();
    }
}
