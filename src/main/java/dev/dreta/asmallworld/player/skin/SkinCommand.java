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

package dev.dreta.asmallworld.player.skin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import dev.dreta.asmallworld.ASmallWorld;
import dev.dreta.asmallworld.player.Camera;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("aswskin")
@CommandPermission("asw.skin.manage")
@Description("Gets/sets the skin of a player's NPC.")
public class SkinCommand extends BaseCommand {
    @Default
    public void get(CommandSender sender, Player player) {
        Camera camera = Camera.get(player);
        sender.sendMessage(ASmallWorld.inst().getMsg().getComponent("skin.get.result",
                "{NAME}", camera.getName(),
                "{DISPLAY_NAME}", camera.getDisplayName(),
                "{SKIN_ID}", camera.getSkin() == -1 ? ASmallWorld.inst().getConf().getInt("skins.default") : camera.getSkin()));
    }

    @Default
    public void set(CommandSender sender, Player player, int skin) {
        if (!ASmallWorld.inst().getConf().contains("skins.skins." + skin)) {
            sender.sendMessage(ASmallWorld.inst().getMsg().getComponent("skin.set.skin-not-found"));
            return;
        }
        Camera camera = Camera.get(player);
        camera.setSkin(skin);
        Location previous = camera.getNpc() != null ? new Location(camera.getNpc().level.getWorld(),
                camera.getNpc().getX(), camera.getNpc().getY(), camera.getNpc().getZ(),
                camera.getNpc().getYHeadRot(), camera.getNpc().getXRot()) : null;
        camera.initNPC(player.getWorld());
        if (camera.getScene() != null) {
            camera.getScene().teleportPlayer(camera, previous);
        }
        sender.sendMessage(ASmallWorld.inst().getMsg().getComponent("skin.set.success",
                "{NAME}", camera.getName(),
                "{DISPLAY_NAME}", camera.getDisplayName(),
                "{SKIN_ID}", skin));
    }
}
