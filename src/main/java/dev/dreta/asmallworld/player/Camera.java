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

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.dreta.asmallworld.ASmallWorld;
import dev.dreta.asmallworld.scene.Scene;
import dev.dreta.asmallworld.utils.configuration.Configuration;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
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

    @Getter
    private LivingEntity npc;
    @Getter
    private boolean npcSpawned;

    @Getter
    @Setter
    private int skin;

    @Getter
    @Setter
    private boolean finishedSetup;

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

    public static Camera get(String name) {
        return byName.get(name);
    }

    public static Camera get(UUID uuid) {
        return byUUID.get(uuid);
    }

    public static Camera get(Player player) {
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
            this.scene.removeCamera(getUniqueId());
        }
        this.scene = scene;
        if (scene != null) {
            this.scene.addCamera(this);
        }
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

    /**
     * Spawn the NPC that is bound to this camera.
     */
    public void spawnNPC() {
        if (npc != null && scene != null) {
            for (UUID uuid : scene.getCameras()) {
                spawnNPC(get(uuid));
            }
            npcSpawned = true;
        }
    }

    /**
     * Spawn the NPC that is bound to this camera
     * for a specific player.
     */
    public void spawnNPC(Camera camera) {
        if (npc instanceof net.minecraft.world.entity.player.Player) {
            camera.sendPacket(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, (ServerPlayer) npc));
            camera.sendPacket(new ClientboundAddPlayerPacket((net.minecraft.world.entity.player.Player) npc));
            // Enable skin layers
            npc.getEntityData().set(new EntityDataAccessor<>(17 /* Displayed Skin Parts */, EntityDataSerializers.BYTE),
                    (byte) 127 /* Display all skin parts */);
            // Prevent NPC from showing in tab list
            Bukkit.getScheduler().runTask(ASmallWorld.inst(), () ->
                    camera.sendPacket(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, (ServerPlayer) npc)));
        } else {
            camera.sendPacket(new ClientboundAddMobPacket(npc));
        }
        camera.sendPacket(new ClientboundSetEntityDataPacket(npc.getId(), npc.getEntityData(), false));
        camera.sendPacket(new ClientboundMoveEntityPacket.Rot(npc.getId(), (byte) (npc.getYHeadRot() / 360 * 256), (byte) (npc.getXRot() / 360 * 256), true));
        camera.sendPacket(new ClientboundRotateHeadPacket(npc, (byte) (npc.getYHeadRot() / 360 * 256)));
    }

    /**
     * Teleport the NPC that's bound to this camera.
     */
    public void teleportNPC() {
        if (npc != null && scene != null) {
            for (UUID uuid : scene.getCameras()) {
                teleportNPC(get(uuid));
            }
        }
    }

    /**
     * Teleport the NPC for a specific player.
     */
    public void teleportNPC(Camera camera) {
        camera.sendPacket(new ClientboundTeleportEntityPacket(npc));
    }

    /**
     * Move the NPC within 8 blocks of the source location.
     */
    public void shortMoveNPC(Location old, Location new_) {
        if (npc != null && scene != null) {
            for (UUID uuid : scene.getCameras()) {
                shortMoveNPC(old, new_, get(uuid));
            }
        }
    }

    /**
     * Move the NPC within 8 blocks of the source location
     * for a specific player.
     */
    public void shortMoveNPC(Location old, Location new_, Camera camera) {
        camera.sendPacket(new ClientboundMoveEntityPacket.Pos(npc.getId(),
                (short) ((new_.getX() * 32 - old.getX() * 32) * 128),
                (short) ((new_.getY() * 32 - old.getY() * 32) * 128),
                (short) ((new_.getZ() * 32 - old.getZ() * 32) * 128),
                npc.isOnGround()));
    }

    /**
     * Rotate the NPC's head and body.
     */
    public void rotateNPC(float yHeadRot, float yBodyRot, float xRot) {
        if (npc != null && scene != null) {
            for (UUID uuid : scene.getCameras()) {
                rotateNPC(yHeadRot, yBodyRot, xRot, get(uuid));
            }
        }
    }

    /**
     * Rotate the NPC's head and body for a specific player.
     */
    public void rotateNPC(float yHeadRot, float yBodyRot, float xRot, Camera camera) {
        camera.sendPacket(new ClientboundMoveEntityPacket.Rot(npc.getId(), (byte) (yBodyRot / 360 * 256), (byte) (xRot / 360 * 256), true));
        camera.sendPacket(new ClientboundRotateHeadPacket(npc, (byte) (yHeadRot / 360 * 256)));
    }

    /**
     * Despawn the NPC that is bound to this camera.
     */
    public void despawnNPC() {
        if (npc != null && scene != null) {
            for (UUID uuid : scene.getCameras()) {
                despawnNPC(get(uuid));
            }
            npcSpawned = false;
        }
    }

    /**
     * Despawn the NPC that is bound to this camera
     * for a specific player.
     */
    public void despawnNPC(Camera camera) {
        camera.sendPacket(new ClientboundRemoveEntitiesPacket(npc.getId()));
    }

    /**
     * Initialize the NPC that is bound to this Camera.
     * <p>
     * If the player has the permission "asw.camera.skin",
     * the NPC will use the player's skin. Otherwise, the player
     * must use a configured default skin.
     */
    public void initNPC(World world) {
        npcSpawned = false;
        if (npc != null) {
            despawnNPC();
        }
        if (hasPermission("asw.camera.skin")) {
            GameProfile profile = new GameProfile(UUID.randomUUID(), player.getName());
            GameProfile realProfile = ((CraftPlayer) player).getHandle().getGameProfile();
            for (Map.Entry<String, Property> entry : realProfile.getProperties().entries()) {
                profile.getProperties().put(entry.getKey(), entry.getValue());
            }
            npc = new ServerPlayer(((CraftServer) Bukkit.getServer()).getHandle().getServer(), ((CraftWorld) world).getHandle(),
                    profile);
        } else {
            GameProfile profile = new GameProfile(UUID.randomUUID(), player.getName());
            int skin = this.skin == -1 ? ASmallWorld.inst().getConf().getInt("skins.default") : this.skin;
            if (!ASmallWorld.inst().getConf().contains("skins.skins." + skin)) {
                throw new IllegalArgumentException("Default skin invalid");
            }
            profile.getProperties().put("textures", new Property("textures",
                    ASmallWorld.inst().getConf().getString("skins.skins." + skin + ".data"),
                    ASmallWorld.inst().getConf().getString("skins.skins." + skin + ".signature")));
            npc = new ServerPlayer(((CraftServer) Bukkit.getServer()).getHandle().getServer(), ((CraftWorld) world).getHandle(),
                    profile);
        }
    }

    public boolean hasPermission(String permission) {
        return ASmallWorld.inst().getPerm().has(player, permission);
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

    public void sendPacket(Packet<?> packet) {
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    /**
     * Sends an invisible medium slime with glowing to this camera
     * at a specific block, highlighting that block.
     *
     * @param location The location of the block to highlight
     * @param duration How long the highlight should last, in <b>ticks</b>.
     */
    public void sendBlockHighlight(Location location, long duration) {
        // Create slime, set size and location.
        // This slime won't move because it is only displayed clientside,
        // and isn't ticked on the server.
        Slime slime = new Slime(EntityType.SLIME, ((CraftWorld) location.getWorld()).getHandle());
        slime.setSize(2 /* same size as a block */, false /* don't regenerate to full health */);
        slime.setXRot(0);
        slime.setYRot(0);
        slime.setYBodyRot(0);
        slime.setYHeadRot(0);
        slime.setPos(location.getBlockX() + 0.5, location.getBlockY(), location.getBlockZ() + 0.5);

        // Make the slime invisible and glowing.
        slime.setInvisible(true);
        slime.setGlowingTag(true);
        slime.collides = false;

        // Send the relevant packets.
        sendPacket(new ClientboundAddMobPacket(slime));
        sendPacket(new ClientboundSetEntityDataPacket(slime.getId() /* Entity ID */, slime.getEntityData(), true /* Remove invalid data */));

        // Schedule removal of the fake entity after the specified duration.
        Bukkit.getScheduler().runTaskLater(ASmallWorld.inst(), () ->
                sendPacket(new ClientboundRemoveEntitiesPacket(slime.getId())), duration);
    }

    public void load() {
        playerData.reload();
        if (playerData.contains("scene")) {
            initNPC(player.getWorld());
            scene = ASmallWorld.inst().getData().getScenes().get(playerData.getInt("scene"));
            scene.teleportPlayer(this, playerData.getLocation("last-location"));
        }
        finishedSetup = playerData.getBoolean("finished-setup");
        skin = playerData.getInt("skin", -1);
    }

    public void save() {
        playerData.set("scene", scene.getId());
        playerData.set("last-location", new Location(npc.getBukkitEntity().getWorld(), npc.getX(), npc.getY(), npc.getZ()));
        playerData.set("finished-setup", finishedSetup);
        playerData.set("skin", skin == -1 ? null : skin);
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
