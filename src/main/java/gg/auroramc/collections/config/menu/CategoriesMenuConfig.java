package gg.auroramc.collections.config.menu;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class CategoriesMenuConfig {
    private String title;
    private FillerItem filler;
    private Map<String, ItemConfig> items;
    private Map<String, ItemConfig> customItems;
    private Integer rows = 6;
    private CollectionMenuConfig.ProgressBar progressBar;

    @Getter
    public static final class FillerItem {
        private Boolean enabled;
        private ItemConfig item;
    }

    // Steps by config-version, only ever appended, each sets config-version itself
    public static List<Consumer<YamlConfiguration>> migrations() {
        return List.of(
                (yaml) -> {
                    yaml.set("config-version", null);
                    yaml.set("progress-bar.length", 10);
                    yaml.set("progress-bar.filled-character", "&a&l■");
                    yaml.set("progress-bar.unfilled-character", "&7&l■");
                    yaml.set("config-version", 1);
                }
        );
    }
}
