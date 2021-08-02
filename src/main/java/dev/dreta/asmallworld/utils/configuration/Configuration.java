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

package dev.dreta.asmallworld.utils.configuration;

import com.google.gson.JsonObject;
import dev.dreta.asmallworld.ASmallWorld;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.*;
import java.util.*;

public class Configuration {
    @Getter
    @Setter
    private File file;
    @Getter
    @Setter
    private FileConfiguration config;

    /**
     * Sets up from an embedded resource.
     *
     * @param resourceName The name of the resource. e.g. config.yml
     * @return The configuration loaded
     */
    public static Configuration loadConfiguration(String resourceName) {
        Configuration config = new Configuration();
        config.load(resourceName);
        return config;
    }

    /**
     * Saves the configuration
     */
    @SneakyThrows
    public void save() {
        this.config.save(this.file);
    }

    /**
     * Reloads the configuration
     */
    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    /**
     * Read from an input stream
     *
     * @param stream The input stream to read from
     * @return The contents
     */
    public String readInputStream(InputStream stream) {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String resp;
        try {
            while ((resp = reader.readLine()) != null) {
                builder.append(resp);
                builder.append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    /**
     * Load this configuration from an inner resource.
     *
     * @param from Name of the inner resource
     */
    public void load(String from) {
        load(from, ASmallWorld.class, ASmallWorld.inst().getDataFolder());
    }

    @SneakyThrows
    public void load(String from, Class<?> clazz, File dataFolder) {
        if (!dataFolder.exists()) dataFolder.mkdirs();
        this.file = new File(dataFolder, from);
        if (!this.file.exists()) {
            if (!this.file.createNewFile()) throw new IOException("File creation failed");
            BufferedWriter writer = new BufferedWriter(new FileWriter(this.file));
            writer.write(readInputStream(clazz.getResourceAsStream("/" + from)));
            writer.flush();
            writer.close();
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public Set<String> getKeys(boolean deep) {
        return config.getKeys(deep);
    }

    public Map<String, Object> getValues(boolean deep) {
        return config.getValues(deep);
    }

    public boolean contains(String path) {
        return config.contains(path);
    }

    public boolean contains(String path, boolean ignoreDefault) {
        return config.contains(path, ignoreDefault);
    }

    public boolean isSet(String path) {
        return config.isSet(path);
    }

    public Object get(String path) {
        return config.get(path);
    }

    public Object get(String path, Object def) {
        return config.get(path, def);
    }

    public void set(String path, Object value) {
        config.set(path, value);
    }

    public ConfigurationSection createSection(String path) {
        return config.createSection(path);
    }

    public ConfigurationSection createSection(String path, Map<?, ?> map) {
        return config.createSection(path, map);
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public double getDouble(String path) {
        return config.getDouble(path);
    }

    public double getDouble(String path, double def) {
        return config.getDouble(path, def);
    }

    public long getLong(String path) {
        return config.getLong(path);
    }

    public long getLong(String path, long def) {
        return config.getLong(path, def);
    }

    public List<?> getList(String path) {
        return config.getList(path);
    }

    public List<?> getList(String path, List<?> def) {
        return config.getList(path, def);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public List<Integer> getIntegerList(String path) {
        return config.getIntegerList(path);
    }

    public List<Boolean> getBooleanList(String path) {
        return config.getBooleanList(path);
    }

    public List<Double> getDoubleList(String path) {
        return config.getDoubleList(path);
    }

    public List<Float> getFloatList(String path) {
        return config.getFloatList(path);
    }

    public List<Long> getLongList(String path) {
        return config.getLongList(path);
    }

    public List<Byte> getByteList(String path) {
        return config.getByteList(path);
    }

    public List<Character> getCharacterList(String path) {
        return config.getCharacterList(path);
    }

    public List<Short> getShortList(String path) {
        return config.getShortList(path);
    }

    public List<Map<?, ?>> getMapList(String path) {
        return config.getMapList(path);
    }

    public Vector getVector(String path) {
        return config.getVector(path);
    }

    public Vector getVector(String path, Vector def) {
        return config.getVector(path, def);
    }

    public OfflinePlayer getOfflinePlayer(String path) {
        return config.getOfflinePlayer(path);
    }

    public OfflinePlayer getOfflinePlayer(String path, OfflinePlayer def) {
        return config.getOfflinePlayer(path, def);
    }

    public ItemStack getItemStack(String path) {
        return config.getItemStack(path);
    }

    public ItemStack getItemStack(String path, ItemStack def) {
        return config.getItemStack(path, def);
    }

    public Color getColor(String path) {
        return config.getColor(path);
    }

    public Color getColor(String path, Color def) {
        return config.getColor(path, def);
    }

    public Location getLocation(String path) {
        return config.getLocation(path);
    }

    public Location getLocation(String path, Location def) {
        return config.getLocation(path, def);
    }

    public ConfigurationSection getSection(String path) {
        return config.getConfigurationSection(path);
    }

    public Component getComponent(String path) {
        if (!config.contains(path)) {
            return null;
        }
        if (config.isString(path)) {
            return LegacyComponentSerializer.legacyAmpersand().deserialize(config.getString(path)
                    .replace(LegacyComponentSerializer.SECTION_CHAR, LegacyComponentSerializer.AMPERSAND_CHAR));
        }
        if (config.isList(path)) {
            List<?> list = config.getList(path);
            if (list.isEmpty()) {
                return Component.empty();
            }
            List<Component> components = new ArrayList<>();
            for (Object o : list) {
                if (o instanceof String) {
                    components.add(LegacyComponentSerializer.legacyAmpersand().deserialize(config.getString(path)
                            .replace(LegacyComponentSerializer.SECTION_CHAR, LegacyComponentSerializer.AMPERSAND_CHAR)));
                } else if (o instanceof Map) {
                    components.add(getSingleComponent((Map<String, Object>) o));
                }
            }
            return Component.join(Component.empty(), components);
        }
        if (config.isConfigurationSection(path)) {
            return getSingleComponent(config.getConfigurationSection(path).getValues(true));
        }
        return null;
    }

    private Map<String, Object> sanitizeValuesMap(Map<String, Object> map) {
        Map<String, Object> newMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection) {
                newMap.put(entry.getKey(), sanitizeValuesMap(((ConfigurationSection) entry.getValue()).getValues(true)));
            } else {
                newMap.put(entry.getKey(), entry.getValue());
            }
        }
        return newMap;
    }

    private Component getSingleComponent(Map<String, Object> map) {
        JsonObject tree = ASmallWorld.gson.toJsonTree(sanitizeValuesMap(map)).getAsJsonObject();
        return GsonComponentSerializer.gson().deserializeFromTree(tree);
    }
}
