package com.example.bigmode;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;

public class storage {
    private static Gson gson = new Gson();

    private static String getWorldName() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.getWorldData().getLevelName().toLowerCase().replaceAll(" ", "_");
        } else {
            return "unknown_world";
        }
    }

    private static Path getConfigFilePath(String name) {
        return Paths.get("./config/bigmode_" + name + "_" + getWorldName() + ".json");
    }

    public static HashSet<UUID> loadInitialItemsData(String name) {
        Path filePath = getConfigFilePath(name);
        try (FileReader reader = new FileReader(filePath.toFile())) {
            Type setType = new TypeToken<HashSet<UUID>>() {
            }.getType();
            return gson.fromJson(reader, setType);
        } catch (IOException e) {
            System.out.println("Error loading initial items data for " + name + ": " + e.getMessage());
        }
        return new HashSet<>();
    }

    public static void saveInitialItemsData(String name, HashSet<UUID> items) {
        Path filePath = getConfigFilePath(name);
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            gson.toJson(items, writer);
        } catch (IOException e) {
            System.out.println("Error saving initial items data for " + name + ": " + e.getMessage());
            // Handle more specific exceptions if needed
        }
    }

}
