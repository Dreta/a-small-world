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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * This listener initializes {@link Camera} objects
 * for all online players.
 */
public class CameraListener implements Listener {
    public CameraListener() {
        Bukkit.getPluginManager().registerEvents(this, ASmallWorld.inst());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Camera camera = new Camera(e.getPlayer());
        Camera.getByName().put(e.getPlayer().getName(), camera);
        Camera.getByUUID().put(e.getPlayer().getUniqueId(), camera);
        camera.load();
        camera.initNPC(e.getPlayer().getWorld());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Camera.getByUUID().get(e.getPlayer().getUniqueId()).save();
        Camera.getByName().remove(e.getPlayer().getName());
        Camera.getByUUID().remove(e.getPlayer().getUniqueId());
    }
}
