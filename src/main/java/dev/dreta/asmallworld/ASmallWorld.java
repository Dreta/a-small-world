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

import co.aikar.commands.PaperCommandManager;
import com.google.gson.Gson;
import dev.dreta.asmallworld.data.DataStore;
import dev.dreta.asmallworld.misc.ReloadCommand;
import dev.dreta.asmallworld.player.CameraListener;
import dev.dreta.asmallworld.scene.Scene;
import dev.dreta.asmallworld.scene.SceneCommand;
import dev.dreta.asmallworld.scene.SceneIDValidator;
import dev.dreta.asmallworld.scene.portal.Portal;
import dev.dreta.asmallworld.scene.portal.PortalCommand;
import dev.dreta.asmallworld.utils.configuration.Configuration;
import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public final class ASmallWorld extends JavaPlugin {
    public static Logger logger;
    private static ASmallWorld inst;

    public static final Gson gson = new Gson();

    @Getter
    private Configuration conf;
    @Getter
    private Configuration msg;
    @Getter
    private DataStore data;

    @Getter
    private Chat chat;

    public ASmallWorld() {
        inst = this;
    }

    public static ASmallWorld inst() {
        return inst;
    }

    @Override
    public void onEnable() {
        logger = Logger.getLogger("Minecraft");

        // Check for dependencies
        if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
            logger.severe("ASW requires Vault to properly function. Please install Vault before continuing.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            logger.severe("ASW requires ProtocolLib to properly function. Please install ProtocolLib before continuing.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Load configuration and data
        ConfigurationSerialization.registerClass(Scene.class);
        ConfigurationSerialization.registerClass(Portal.class);

        conf = Configuration.loadConfiguration("config.yml");
        msg = Configuration.loadConfiguration("messages.yml");
        data = new DataStore();

        // Set up player data
        File playerData = new File(getDataFolder(), "playerData");
        if (!playerData.exists()) {
            if (!playerData.mkdirs()) {
                logger.severe("ASW failed to create player data directory. Might be permissions issue?");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }

        // Load Vault
        if (getServer().getServicesManager().getRegistration(Chat.class) != null) {
            chat = getServer().getServicesManager().getRegistration(Chat.class).getProvider();
        } else {
            logger.severe("ASW requires vault-compatible permission plugin serving chats.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register commands
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.enableUnstableAPI("brigadier");
        manager.getCommandConditions().addCondition(int.class, "sceneexist", new SceneIDValidator());
        manager.getCommandConditions().addCondition(Integer.class, "sceneexist", new SceneIDValidator());
        manager.registerCommand(new ReloadCommand());
        manager.registerCommand(new SceneCommand());
        manager.registerCommand(new PortalCommand());

        // Register listeners
        new CameraListener();

        logger.info("Hello, a small world!\nSuccessfully enabled ASW.");
    }

    @Override
    public void onDisable() {
        data.saveScenes();
        logger.info("Goodbye, small world!\nASW is now disabled.");
    }
}
