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

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import dev.dreta.asmallworld.ASmallWorld;
import dev.dreta.asmallworld.player.Camera;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@CommandAlias("aswscene")
@CommandPermission("asw.scene.manage")
public class SceneCommand extends BaseCommand {
    private static final int PAGE_ITEM_AMOUNT = 5;

    @Subcommand("create")
    @Description("Creates a scene at your current location.")
    public void create(Player player, String name) {
        Location loc = player.getLocation();
        synchronized (Scene.createLock) {
            Scene scene = new Scene(name, loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), new ArrayList<>());
            player.sendMessage(ASmallWorld.inst().getMsg().getComponent("scene.create.generation"));
            scene.generateWalls();
            scene.generatePlayerArea();
            ASmallWorld.inst().getData().getScenes().put(scene.getId(), scene);
            ASmallWorld.inst().getData().save();
            player.sendMessage(
                    ASmallWorld.inst().getMsg().getComponent("scene.create.success",
                            "{SCENE_ID}", scene.getId(),
                            "{SCENE_NAME}", name,
                            "{SCENE_WORLD}", loc.getWorld().getName(),
                            "{SCENE_X}", loc.getBlockX(),
                            "{SCENE_Y}", loc.getBlockY(),
                            "{SCENE_Z}", loc.getBlockZ()));
        }
    }

    @Subcommand("unregister")
    @Description("Unregisters a scene.")
    public void unregister(CommandSender sender, @Conditions("sceneexist") int id) {
        Scene scene = ASmallWorld.inst().getData().getScenes().remove(id);
        ASmallWorld.inst().getData().save();
        sender.sendMessage(
                ASmallWorld.inst().getMsg().getComponent("scene.unregister.success",
                        "{SCENE_ID}", scene.getId(),
                        "{SCENE_NAME}", scene.getName()));
    }

    @Subcommand("list")
    @Description("Lists all available scenes.")
    public void list(CommandSender sender) {
        list(sender, 1);
    }

    @Subcommand("list")
    @Description("Lists all available scenes at a specific page.")
    public void list(CommandSender sender, int page) {
        if (ASmallWorld.inst().getData().getScenes().isEmpty()) {
            sender.sendMessage(ASmallWorld.inst().getMsg().getComponent("scene.list.fail-empty"));
            return;
        }
        List<Scene> scenes = ASmallWorld.inst().getData().getScenes().values().stream().sorted(Comparator.comparingInt(Scene::getId)).collect(Collectors.toList());
        int total = (int) Math.ceil(scenes.size() / (double) PAGE_ITEM_AMOUNT);
        Component component = Component.empty();
        for (Scene scene : scenes.subList(PAGE_ITEM_AMOUNT * (page - 1), Math.min(scenes.size(), PAGE_ITEM_AMOUNT * page))) {
            component = component.append(ASmallWorld.inst().getMsg().getComponent("scene.list.page-item",
                    "{SCENE_NAME}", scene.getName(),
                    "{SCENE_ID}", scene.getId(),
                    "{SCENE_WORLD}", scene.getWorld().getName(),
                    "{SCENE_X}", scene.getX(),
                    "{SCENE_Y}", scene.getY(),
                    "{SCENE_Z}", scene.getZ())).append(Component.text("\n"));
        }
        sender.sendMessage(ASmallWorld.inst().getMsg().getComponent("scene.list.page",
                "{SCENES}", component,
                "{PAGE}", page,
                "{TOTAL_PAGES}", total,
                "{PREVIOUS_PAGE_ARROW}", page == 1 ? "   " : "[<]",
                "{NEXT_PAGE_ARROW}", page == total ? "   " : "[>]",
                "{PREVIOUS_PAGE}", page - 1,
                "{NEXT_PAGE}", page + 1));
    }

    @Subcommand("teleport")
    @Description("Teleports yourself to a scene.")
    public void teleport(Player player, @Conditions("sceneexist") int id) {
        Scene scene = ASmallWorld.inst().getData().getScenes().get(id);
        Camera camera = Camera.getCamera(player);
        camera.despawnNPC();
        camera.setScene(scene);
        scene.teleportPlayer(camera);
    }

    @Subcommand("teleport")
    @Description("Teleports another player to a scene.")
    public void teleport(CommandSender sender, Player target, @Conditions("sceneexist") int id) {
        Scene scene = ASmallWorld.inst().getData().getScenes().get(id);
        Camera camera = Camera.getCamera(target);
        camera.despawnNPC();
        camera.setScene(scene);
        scene.teleportPlayer(camera);
        sender.sendMessage(ASmallWorld.inst().getMsg().getComponent("scene.teleport.success-other",
                "{TARGET_NAME}", camera.getName(),
                "{TARGET_DISPLAY_NAME}", camera.getDisplayName(),
                "{TARGET_SCENE_NAME}", scene.getName()));
    }
}
