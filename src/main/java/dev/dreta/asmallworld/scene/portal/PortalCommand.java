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

package dev.dreta.asmallworld.scene.portal;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import dev.dreta.asmallworld.ASmallWorld;
import dev.dreta.asmallworld.player.Camera;
import dev.dreta.asmallworld.scene.Scene;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

@CommandAlias("aswportal")
@CommandPermission("asw.portal")
public class PortalCommand extends BaseCommand {
    // The players that requested to create a scene, but haven't
    // specified the target location yet.
    private static final Map<UUID, Location> creating = new HashMap<>();

    private static final int PAGE_ITEM_AMOUNT = 5;

    /**
     * Sends an invisible medium slime with glowing to a player
     * at a specific block, highlighting that block.
     *
     * @param player   The player to send the entity to
     * @param location The location of the block to highlight
     * @param duration How long the highlight should last, in <b>ticks</b>.
     */
    private static void sendBlockHighlight(Player player, Location location, long duration) {
        Camera camera = Camera.getCamera(player);

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
        camera.sendPacket(new ClientboundAddMobPacket(slime));
        camera.sendPacket(new ClientboundSetEntityDataPacket(slime.getId() /* Entity ID */, slime.getEntityData(), true /* Remove invalid data */));

        // Schedule removal of the fake entity after the specified duration.
        Bukkit.getScheduler().runTaskLater(ASmallWorld.inst(), () ->
                camera.sendPacket(new ClientboundRemoveEntitiesPacket(slime.getId())), duration);
    }

    @Subcommand("highlight")
    @Description("Highlight where the portal is within a scene.")
    public void highlight(Player player, @Conditions("sceneexist") int sceneID, int portalID) {
        Scene scene = ASmallWorld.inst().getData().getScenes().get(sceneID);
        if (!scene.getPortals().containsKey(portalID)) {
            player.sendMessage(ASmallWorld.inst().getMsg().getComponent("portal.highlight.portal-dont-exist"));
            return;
        }

        Portal portal = scene.getPortals().get(portalID);
        int duration = ASmallWorld.inst().getConf().getInt("scene.portal.highlight-duration");
        for (Location location : portal.getBlocks()) {
            sendBlockHighlight(player, location, duration * 20L);
        }

        player.sendMessage(ASmallWorld.inst().getMsg().getComponent("portal.highlight.success",
                "{HIGHLIGHT_SECONDS}", duration));
    }

    @Subcommand("cancel")
    @Description("Cancel the creation of a new portal.")
    public void cancel(Player player) {
        if (creating.remove(player.getUniqueId()) != null) {
            player.sendMessage(ASmallWorld.inst().getMsg().getComponent("portal.create.cancel"));
        }
    }

    @Subcommand("create")
    @Description("Creates a portal at your current location.")
    public void create(Player player, @Conditions("sceneexist") int sourceScene, @Conditions("sceneexist") int targetScene) {
        Scene source = ASmallWorld.inst().getData().getScenes().get(sourceScene);
        Scene target = ASmallWorld.inst().getData().getScenes().get(targetScene);
        if (creating.containsKey(player.getUniqueId())) {
            // The player clicked the previous message to set the target location.
            // The player is currently in the target scene.
            Location sourceLoc = creating.remove(player.getUniqueId());
            Location targetLoc = player.getLocation();
            if (!target.contains(targetLoc)) {
                player.sendMessage(ASmallWorld.inst().getMsg().getComponent("portal.create.not-in-target-scene"));
                return;
            }
            synchronized (Portal.createLock) {
                Portal portal = new Portal(sourceScene, new ArrayList<>(Collections.singletonList(sourceLoc)), targetScene, targetLoc, false);
                source.getPortals().put(portal.getId(), portal);
                ASmallWorld.inst().getData().getPortals().put(portal.getId(), portal);
                ASmallWorld.inst().getData().save();
                player.sendMessage(ASmallWorld.inst().getMsg().getComponent("portal.create.success",
                        "{ID}", portal.getId(),
                        "{SOURCE_ID}", sourceScene,
                        "{SOURCE_NAME}", source.getName(),
                        "{SOURCE_X}", sourceLoc.getBlockX(),
                        "{SOURCE_Y}", sourceLoc.getBlockY(),
                        "{SOURCE_Z}", sourceLoc.getBlockZ(),
                        "{TARGET_ID}", targetScene,
                        "{TARGET_NAME}", target.getName(),
                        "{TARGET_X}", targetLoc.getBlockX(),
                        "{TARGET_Y}", targetLoc.getBlockY(),
                        "{TARGET_Z}", targetLoc.getBlockZ()));
            }
            return;
        }
        // The player executed the command by themselves.
        // The player is currently in the source scene.
        if (!source.contains(player.getLocation())) {
            player.sendMessage(ASmallWorld.inst().getMsg().getComponent("portal.create.not-in-source-scene"));
            return;
        }
        creating.put(player.getUniqueId(), player.getLocation());
        player.sendMessage(ASmallWorld.inst().getMsg().getComponent("portal.create.go-to-target-scene",
                "{SOURCE}", sourceScene,
                "{TARGET}", targetScene));
    }

    @Subcommand("unregister")
    @Description("Unregisters a portal within a scene.")
    public void unregister(CommandSender sender, @Conditions("sceneexist") int sceneID, int portalID) {
        Scene scene = ASmallWorld.inst().getData().getScenes().get(sceneID);
        if (!scene.getPortals().containsKey(portalID)) {
            sender.sendMessage(ASmallWorld.inst().getMsg().getComponent("portal.unregister.portal-dont-exist"));
            return;
        }
        scene.getPortals().remove(portalID);
        ASmallWorld.inst().getData().getPortals().remove(portalID);
        ASmallWorld.inst().getData().save();
        sender.sendMessage(ASmallWorld.inst().getMsg().getComponent("portal.unregister.success",
                "{ID}", portalID,
                "{SCENE_ID}", sceneID));
    }

    @Subcommand("list")
    @Description("Lists all available portals within a scene.")
    public void list(CommandSender sender, @Conditions("sceneexist") int sceneID) {
        list(sender, 1, sceneID);
    }

    @Subcommand("list")
    @Description("Lists all available portals at a specific page.")
    public void list(CommandSender sender, int page, @Conditions("sceneexist") int sceneID) {
        Scene scene = ASmallWorld.inst().getData().getScenes().get(sceneID);
        if (scene.getPortals().isEmpty()) {
            sender.sendMessage(ASmallWorld.inst().getMsg().getComponent("portal.list.fail-empty"));
            return;
        }
        List<Portal> portals = scene.getPortals().values().stream().sorted(Comparator.comparingInt(Portal::getId)).collect(Collectors.toList());
        int total = (int) Math.ceil(portals.size() / (double) PAGE_ITEM_AMOUNT);
        Component component = Component.empty();
        for (Portal portal : portals.subList(PAGE_ITEM_AMOUNT * (page - 1), Math.min(portals.size(), PAGE_ITEM_AMOUNT * page))) {
            component = component.append(ASmallWorld.inst().getMsg().getComponent("portal.list.page-item",
                    "{ID}", portal.getId(),
                    "{TARGET_SCENE_ID}", portal.getTarget().getId(),
                    "{TARGET_SCENE_NAME}", portal.getTarget().getName(),
                    "{TARGET_X}", portal.getTargetLocation().getBlockX(),
                    "{TARGET_Y}", portal.getTargetLocation().getBlockY(),
                    "{TARGET_Z}", portal.getTargetLocation().getBlockZ())).append(Component.text("\n"));
        }
        sender.sendMessage(ASmallWorld.inst().getMsg().getComponent("portal.list.page",
                "{PORTALS}", component,
                "{SCENE_NAME}", scene.getName(),
                "{SCENE_ID}", sceneID,
                "{PAGE}", page,
                "{TOTAL_PAGES}", total,
                "{PREVIOUS_PAGE_ARROW}", page == 1 ? "   " : "[<]",
                "{NEXT_PAGE_ARROW}", page == total ? "   " : "[>]",
                "{PREVIOUS_PAGE}", page - 1,
                "{NEXT_PAGE}", page + 1));
    }
}
