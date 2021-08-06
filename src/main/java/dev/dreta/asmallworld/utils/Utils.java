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

package dev.dreta.asmallworld.utils;

import org.bukkit.Location;

public class Utils {
    private Utils() {
    }

    /**
     * Creates a hash of a location.
     * This hash function ONLY CONSIDERS the world, BLOCK X, BLOCK Y and BLOCK Z.
     *
     * @param loc The location to hash
     * @return The unique hash code
     */
    public static int hashLocationToBlock(Location loc) {
        int hash = 3;
        hash = 19 * hash + (loc.getWorld() == null ? 0 : loc.getWorld().hashCode());
        hash = 19 * hash + (int) (Integer.toUnsignedLong(loc.getBlockX()) ^ (Integer.toUnsignedLong(loc.getBlockX()) >>> 32));
        hash = 19 * hash + (int) (Integer.toUnsignedLong(loc.getBlockY()) ^ (Integer.toUnsignedLong(loc.getBlockY()) >>> 32));
        hash = 19 * hash + (int) (Integer.toUnsignedLong(loc.getBlockZ()) ^ (Integer.toUnsignedLong(loc.getBlockZ()) >>> 32));
        return hash;
    }
}
