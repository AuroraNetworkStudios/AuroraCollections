package gg.auroramc.collections.config;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class CategoriesConfig {
    private Map<String, CategoryConfig> categories;

    @Getter
    public static class CategoryConfig {
        private String name;
        private String permission;
        private Map<String, LevelConfig> levels;
    }

    @Getter
    public static class LevelConfig {
        private Double percentage;
        private Map<String, ItemConfig> item;
        private ConfigurationSection rewards;
    }

    // Steps by config-version, only ever appended, each sets config-version itself
    public static List<Consumer<YamlConfiguration>> migrations() {
        return List.of(
                (yaml) -> {
                    var current = new HashMap<String, String>();
                    var comments = new HashMap<String, List<String>>();
                    var mainComments = yaml.getComments("categories");

                    for (var key : yaml.getConfigurationSection("categories").getKeys(false)) {
                        current.put(key, yaml.getString("categories." + key, ""));
                        comments.put(key, yaml.getComments("categories." + key));
                    }

                    yaml.set("categories", null);
                    yaml.set("config-version", 1);

                    var categories = yaml.createSection("categories");
                    yaml.setComments("categories", mainComments);

                    for (var entry : current.entrySet()) {
                        var category = categories.createSection(entry.getKey());
                        category.set("name", entry.getValue());
                        yaml.setComments("categories." + entry.getKey(), comments.get(entry.getKey()));
                    }
                }
        );
    }
}
