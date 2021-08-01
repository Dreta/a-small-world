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

package dev.dreta.asmallworld.test;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import dev.dreta.asmallworld.scene.Scene;
import org.bukkit.entity.Player;

/**
 * This command allows a tester to test all the features
 * of {@link dev.dreta.asmallworld.scene.Scene}.
 */
@CommandAlias("scenetest")
@CommandPermission("asw.tester")
public class SceneTestCommand extends BaseCommand {
    @Default
    public void test(Player player) {
        Scene scene = new Scene(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
        scene.generateWalls();
        scene.generatePlayerArea();
        scene.teleportPlayer(player);
    }
}
