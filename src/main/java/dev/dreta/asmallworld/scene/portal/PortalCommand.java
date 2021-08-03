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

import co.aikar.commands.annotation.*;
import dev.dreta.asmallworld.ASmallWorld;
import dev.dreta.asmallworld.scene.Scene;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

@CommandAlias("aswportal")
@CommandPermission("asw.portal")
public class PortalCommand {
    // The players that requested to create a scene, but haven't
    // specified the target location yet.
    private static final Map<UUID, Location> creating = new HashMap<>();

    @Subcommand("create")
    @Description("Creates a portal at your current location.")
    public void create(Player player, @Conditions("sceneExist") int sourceScene, @Conditions("sceneExist") int targetScene) {
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
    public void unregister(CommandSender sender, @Conditions("sceneExist") int sceneID, int portalID) {
        Scene scene = ASmallWorld.inst().getData().getScenes().get(sceneID);
        if (!scene.getPortals().containsKey(portalID)) {
            sender.sendMessage(ASmallWorld.inst().getMsg().getComponent("portal.unregister.portal-dont-exist"));
            return;
        }
        scene.getPortals().remove(portalID);
        ASmallWorld.inst().getData().save();
        sender.sendMessage(ASmallWorld.inst().getMsg().getComponent("portal.unregister.success",
                "{ID}", portalID,
                "{SCENE_ID}", sceneID));
    }
}
