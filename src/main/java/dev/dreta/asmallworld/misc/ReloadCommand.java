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

package dev.dreta.asmallworld.misc;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import dev.dreta.asmallworld.ASmallWorld;
import dev.dreta.asmallworld.player.Camera;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("aswreload")
@CommandPermission("asw.manage")
public class ReloadCommand extends BaseCommand {
    @Default
    public void reload(CommandSender sender) {
        ASmallWorld.inst().getConf().reload();
        ASmallWorld.inst().getMsg().reload();
        sender.sendMessage(ASmallWorld.inst().getMsg().getComponent("reload.core-reloaded"));
    }

    @Subcommand("player")
    private static class PlayerSub extends BaseCommand {
        @Default
        public void warn(CommandSender sender) {
            sender.sendMessage(ASmallWorld.inst().getMsg().getComponent("reload.player.warning"));
        }

        @Subcommand("confirm")
        public void player(CommandSender sender, Player player) {
            Camera camera = Camera.get(player);
            camera.load();
            sender.sendMessage(ASmallWorld.inst().getMsg().getComponent("reload.player.success",
                    "{NAME}", player.getName(),
                    "{DISPLAY_NAME}", camera.getDisplayName()));
        }
    }
}
