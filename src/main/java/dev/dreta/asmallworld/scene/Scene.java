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

package dev.dreta.asmallworld.scene;

import dev.dreta.asmallworld.ASmallWorld;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * Scenes are the basic building blocks of ASW.
 * <p>
 * Users of ASW can create different scenes that include
 * a small part of the world. Each scene contains an 21x(21+5)x34
 * area with walls surrounding them.
 * <p>
 * When a player is inside a scene, they will be teleported to
 * the near-top area of extra 5 blocks part of the scene,
 * where they can see the entire scene from above. Then, they
 * will be able to use normal controls to control an NPC, which
 * will represent them in the scene.
 * <p>
 * The player will be able to control their characters to
 * pass through the specified doors to enter the scene that's
 * linked to the door.
 * <p>
 * The thickness of the wall of each scene is 2 blocks.
 * <p>
 * NOTE: All the directions (left/right etc.) mentioned in the
 * comments are relative to the player control area.
 */
public class Scene implements ConfigurationSerializable {
    public static final int LENGTH = 21;
    public static final int WIDTH = 21;
    public static final int FULL_WIDTH = WIDTH + 5;
    public static final int HEIGHT = 34;

    /**
     * The world that this scene is in.
     */
    @Getter
    private final World world;

    /**
     * The block X coordinate of the top-left bottom-most block of the scene.
     * <p>
     * This block should be a wall block.
     */
    @Getter
    private final int x;

    /**
     * The block Y coordinate of the top-left bottom-most block of the scene.
     * <p>
     * This block should be a wall block.
     */
    @Getter
    private final int y;

    /**
     * The block Z coordinate of the top-left bottom-most block of the scene.
     * <p>
     * This block should be a wall block.
     */
    @Getter
    private final int z;

    public Scene(World world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Scene deserialize(Map<String, Object> map) {
        return new Scene(Bukkit.getWorld(UUID.fromString((String) map.get("world"))), (int) map.get("x"), (int) map.get("y"), (int) map.get("z"));
    }

    /**
     * Generate the walls of this scene.
     */
    public void generateWalls() {
        Material type = Material.valueOf(ASmallWorld.inst().getConf().getString("wall"));

        // Left wall
        for (int z = this.z; z <= this.z + FULL_WIDTH + 2; z++) {
            for (int y = this.y; y <= this.y + HEIGHT; y++) {
                world.getBlockAt(x, y, z).setType(type);
                world.getBlockAt(x + 1, y, z).setType(type);
            }
        }

        // Right wall
        for (int z = this.z; z <= this.z + FULL_WIDTH + 2; z++) {
            for (int y = this.y; y <= this.y + HEIGHT; y++) {
                world.getBlockAt(x + LENGTH + 2, y, z).setType(type);
                world.getBlockAt(x + LENGTH + 2 + 1, y, z).setType(type);
            }
        }

        // Front wall
        for (int x = this.x; x <= this.x + LENGTH + 2; x++) {
            for (int y = this.y; y <= this.y + HEIGHT; y++) {
                world.getBlockAt(x, y, z).setType(type);
                world.getBlockAt(x, y, z + 1).setType(type);
            }
        }

        // Back wall
        for (int x = this.x; x <= this.x + LENGTH + 2; x++) {
            for (int y = this.y; y <= this.y + HEIGHT; y++) {
                world.getBlockAt(x, y, z + FULL_WIDTH + 2).setType(type);
                world.getBlockAt(x, y, z + FULL_WIDTH + 2 + 1).setType(type);
            }
        }

        // Block the unusable space
        for (int x = this.x + 2; x <= this.x + 1 + LENGTH; x++) {
            for (int z = this.z + WIDTH + 1; z <= this.z + WIDTH + 5 + 1; z++) {
                world.getBlockAt(x, this.y, z).setType(type);
                world.getBlockAt(x, this.y + 1, z).setType(type);
            }
        }
    }

    /**
     * Generate the area where the player always stays in.
     */
    public void generatePlayerArea() {
        for (int x = this.x + 11; x <= this.x + 13; x++) {
            for (int y : new Integer[]{this.y + HEIGHT - 3, this.y + HEIGHT}) {
                for (int z = this.z + FULL_WIDTH - 1; z <= this.z + FULL_WIDTH + 2 - 1; z++) {
                    world.getBlockAt(x, y, z).setType(Material.BARRIER);
                }
            }
        }
    }

    /**
     * Teleport a player to the player area.
     *
     * @param player The player to teleport
     */
    public void teleportPlayer(Player player) {
        player.teleport(new Location(world, x + 12 + 0.5, y + HEIGHT - 2, z + FULL_WIDTH + 0.5, 180, 50));
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of("world", world.getUID().toString(), "x", x, "y", y, "z", z);
    }
}
