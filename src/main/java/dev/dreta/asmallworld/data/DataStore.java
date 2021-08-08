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

package dev.dreta.asmallworld.data;

import dev.dreta.asmallworld.ASmallWorld;
import dev.dreta.asmallworld.player.Camera;
import dev.dreta.asmallworld.scene.Scene;
import dev.dreta.asmallworld.scene.portal.Portal;
import dev.dreta.asmallworld.utils.configuration.Configuration;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * DataStore manages the data stored by ASW.
 */
public class DataStore extends Configuration {
    @Getter
    private final Map<Integer, Scene> scenes = new HashMap<>();
    @Getter
    private final Map<Integer, Portal> portals = new HashMap<>();

    public DataStore() {
        this.load("data.yml");
        loadScenes();
        // Auto save
        Bukkit.getScheduler().runTaskTimer(ASmallWorld.inst(), () -> {
            ASmallWorld.logger.info("Saving ASW data.");
            save();
        }, 600 * 20L, 600 * 20L);
    }

    public void loadScenes() {
        scenes.clear();
        for (Object scene : getList("scenes", Collections.emptyList())) {
            scenes.put(((Scene) scene).getId(), (Scene) scene);
            for (Portal portal : ((Scene) scene).getPortals().values()) {
                portals.put(portal.getId(), portal);
            }
        }
    }

    public void saveScenes() {
        set("scenes", null);
        set("scenes", new ArrayList<>(scenes.values()));
    }

    @Override
    public void save() {
        saveScenes();
        for (Camera camera : Camera.getByUUID().values()) {
            camera.save();
        }
        super.save();
    }
}
