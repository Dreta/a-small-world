package dev.dreta.asmallworld.utils.configuration;

import dev.dreta.asmallworld.ASmallWorld;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

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
}
