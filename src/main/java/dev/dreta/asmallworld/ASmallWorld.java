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

package dev.dreta.asmallworld;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class ASmallWorld extends JavaPlugin {
    public static Logger logger;
    private static ASmallWorld inst;

    public ASmallWorld() {
        inst = this;
    }

    public static ASmallWorld inst() {
        return inst;
    }

    @Override
    public void onEnable() {
        logger = Logger.getLogger("Minecraft");
        logger.info("Hello, world!");
    }

    @Override
    public void onDisable() {
        logger.info("Goodbye, world!");
    }
}
