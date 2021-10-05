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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class CameraMovementListener implements Listener {
    public static int MOVE_SPEED_SCALE = 5;

    public CameraMovementListener() {
        Bukkit.getPluginManager().registerEvents(this, ASmallWorld.inst());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Camera camera = Camera.get(e.getPlayer());
        if (camera.getScene() == null || !camera.isNpcSpawned()) {
            return; // Don't handle players that aren't in a scene.
        }
        // TODO Save NPC X/Y/Z/Y Head Rot/X Rot and set them on load
        // FIXME NPC "half-stuck" in the border wall when colliding
        // Handle typical moving (x/z)
        double offsetX = e.getTo().getX() - e.getFrom().getX();
        double offsetZ = e.getTo().getZ() - e.getFrom().getZ();
        if (offsetX != 0 || offsetZ != 0) {
            Location sceneTo = new Location(camera.getScene().getWorld(),
                    camera.getNpc().getX() + offsetX * MOVE_SPEED_SCALE,
                    camera.getNpc().getY(),
                    camera.getNpc().getZ() + offsetZ * MOVE_SPEED_SCALE);

            // Rotate the NPC accordingly to the new direction that the NPC
            // is walking in.
            float yHeadRot = 0;
            float xRot = 0;
            if (offsetX > 0) {
                yHeadRot = -90;
            } else if (offsetX < 0) {
                yHeadRot = 90;
            } else if (offsetZ < 0) {
                yHeadRot = 180;
            }
            camera.rotateNPC(yHeadRot, xRot);
            // Set the proper rotation on the NPC object itself
            camera.getNpc().setYHeadRot(yHeadRot);
            camera.getNpc().setXRot(xRot);

            // Order matters. We must rotate first and move later
            // (presumably because of some implementation details),
            // or the NPC won't move.
            if (sceneTo.getBlock().getType() == Material.AIR || sceneTo.getBlock().getType() == Material.CAVE_AIR) {
                // Move the NPC if the target isn't blocked
                camera.shortMoveNPC(camera.getNpc().getBukkitEntity().getLocation(), sceneTo);
                // Set the proper values on the NPC object itself
                camera.getNpc().setPos(sceneTo.getX(), sceneTo.getY(), sceneTo.getZ());
            }
        }

        e.setCancelled(true); // Prevent the player from actually moving
    }
}
