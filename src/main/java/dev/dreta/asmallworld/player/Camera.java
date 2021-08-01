/*
 * A Small World is a curated 2.5D Minecraft experience.
 * Copyright (C) 2021 Dreta / Gabriel Leen
 *
 * A Small World is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * A Small World is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with A Small World.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.dreta.asmallworld.player;

import dev.dreta.asmallworld.ASmallWorld;
import dev.dreta.asmallworld.scene.Scene;
import dev.dreta.asmallworld.utils.configuration.Configuration;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A camera represents a {@link Player} that is inside a scene.
 * Each camera is linked to an NPC within the scene that the player
 * can normally control. ASW controls every aspect of the player's
 * movement and interactions when a player is within a scene through
 * a camera.
 */
public class Camera {
    @Getter
    private static final Map<String, Camera> byName = new HashMap<>();

    @Getter
    private static final Map<UUID, Camera> byUUID = new HashMap<>();
    @Getter
    private final Player player;
    @Getter
    private final Configuration playerData;
    @Getter
    private Scene scene;

    @SneakyThrows
    public Camera(Player player) {
        this.player = player;
        playerData = new Configuration();
        File file = new File(ASmallWorld.inst().getDataFolder(), "playerData/" + player.getUniqueId() + ".yml");
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("Failed to create player data file for " + player.getName());
            }
        }
        playerData.setFile(file);
        playerData.setConfig(YamlConfiguration.loadConfiguration(file));
    }

    public static Camera getCamera(String name) {
        return byName.get(name);
    }

    public static Camera getCamera(UUID uuid) {
        return byUUID.get(uuid);
    }

    public static Camera getCamera(Player player) {
        Camera camera = byUUID.get(player.getUniqueId());
        if (camera == null) {
            Camera cam = new Camera(player);
            byName.put(player.getName(), cam);
            byUUID.put(player.getUniqueId(), cam);
            return cam;
        }
        return camera;
    }

    public void setScene(Scene scene) {
        if (this.scene != null) {
            this.scene.getCameras().remove(getUniqueId());
        }
        this.scene = scene;
        scene.getCameras().add(getUniqueId());
    }

    public String getName() {
        return player.getName();
    }

    public Component getDisplayName() {
        return Component.text()
                .append(Component.text(ASmallWorld.inst().getChat().getPlayerPrefix(player)))
                .append(player.displayName())
                .append(Component.text(ASmallWorld.inst().getChat().getPlayerSuffix(player)))
                .build();
    }

    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    public void sendMessage(Component component) {
        player.sendMessage(component);
    }

    public void sendMessage(Identity source, Component component) {
        player.sendMessage(source, component);
    }

    public void sendMessage(Identified source, Component component) {
        player.sendMessage(source, component);
    }

    public void load() {
        scene = ASmallWorld.inst().getData().getScenes().get(playerData.getInt("scene"));
        scene.getCameras().add(getUniqueId());
    }

    public void save() {
        playerData.set("scene", scene.getId());
        playerData.save();
    }

    @Override
    public String toString() {
        return getClass().getName() + " { name = " + getName() + ", uuid = " + getUniqueId().toString() + " }";
    }

    @Override
    public boolean equals(Object b) {
        if (!(b instanceof Camera)) {
            return false;
        }
        return getUniqueId().equals(((Camera) b).getUniqueId());
    }

    @Override
    public int hashCode() {
        return getUniqueId().hashCode() + 3840;
    }
}
