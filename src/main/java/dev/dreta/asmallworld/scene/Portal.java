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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Users of ASW may add "portals" in {@link Scene}s at
 * different locations that teleports the player to a
 * different scene.
 * <p>
 * Each "portal" can be associated with a set of blocks,
 * and when the player-controlled NPC reaches the specified
 * blocks, the player and the NPC will be teleported to the
 * specified location in a different scene.
 */
public class Portal {
    @Getter
    private final int id;

    /**
     * The ID of the associated scene of this portal.
     */
    private final int scene;

    /**
     * The blocks included with this portal.
     */
    @Getter
    private final List<Location> blocks;

    /**
     * The ID of the target scene of this portal.
     */
    private final int target;

    /**
     * The target location of this portal (within the target scene).
     */
    @Getter
    private final Location targetLocation;

    /**
     * Whether the "load screen" should be used for this portal.
     */
    @Getter
    private final boolean useLoadScreen;

    /**
     * Maps strings of player UUID to the teleportation task
     * that is in progress for that player.
     */
    private final Map<String, BukkitTask> tpTasks = new HashMap<>();

    public Portal(int scene, List<Location> blocks, int target, Location targetLocation, boolean useLoadScreen) {
        this(ASmallWorld.inst().getData().getPortals().keySet().stream().max(Integer::compareTo).get() + 1,
                scene, blocks, target, targetLocation, useLoadScreen);
    }

    private Portal(int id, int scene, List<Location> blocks, int target, Location targetLocation, boolean useLoadScreen) {
        this.id = id;
        this.scene = scene;
        this.blocks = new ArrayList<>(blocks); // Ensure that this list is modifiable
        this.target = target;
        this.targetLocation = targetLocation;
        this.useLoadScreen = useLoadScreen;
    }

    /**
     * Begin the teleportation process when the player entered
     * this portal.
     *
     * @param player The player to teleport
     */
    public void beginTeleportation(Player player) {
        tpTasks.put(player.getUniqueId().toString(), Bukkit.getScheduler().runTaskLater(ASmallWorld.inst(),
                () -> {
                    tpTasks.remove(player.getUniqueId().toString());
                    if (useLoadScreen) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 10, false, false, false));
                        // TODO Placeholders
                        player.showTitle(Title.title(Component.text(ASmallWorld.inst().getConf().getString("scene.portal.load-screen.title.title")),
                                Component.text(ASmallWorld.inst().getConf().getString("scene.portal.load-screen.title.subtitle")), Title.Times.of(Ticks.duration(0), Ticks.duration(999999), Ticks.duration(0))));
                        player.sendActionBar(Component.text(ASmallWorld.inst().getConf().getString("scene.portal.load-screen.title.actionbar")));
                        Bukkit.getScheduler().runTaskLater(ASmallWorld.inst(), () -> {
                            player.removePotionEffect(PotionEffectType.BLINDNESS);
                            player.clearTitle();
                            getTarget().teleportPlayer(player);
                        }, ASmallWorld.inst().getConf().getInt("scene.portal.load-screen.duration") / 100 * 20);
                    } else {
                        getTarget().teleportPlayer(player);
                    }
                }, ASmallWorld.inst().getConf().getInt("scene.portal.delay") / 1000 * 20));
    }

    /**
     * If a player left this portal before the teleportation
     * commenced, cancel the teleportation task.
     *
     * @param player The player to cancel the task for.
     */
    public void cancelTeleportation(Player player) {
        if (tpTasks.containsKey(player.getUniqueId().toString())) {
            tpTasks.remove(player.getUniqueId().toString()).cancel();
        }
    }

    public Scene getScene() {
        return ASmallWorld.inst().getData().getScenes().get(scene);
    }

    public Scene getTarget() {
        return ASmallWorld.inst().getData().getScenes().get(target);
    }
}
