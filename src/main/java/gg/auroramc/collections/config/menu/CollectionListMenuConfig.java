package gg.auroramc.collections.config.menu;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class CollectionListMenuConfig {
    private String title;
    private Map<String, ItemConfig> customItems;
    private List<Integer> displayArea;
    private Items items;
    private Integer rows = 6;
    private CategoryIcon categoryIcon;
    private SecretCollectionDisplay secretCollectionDisplay = new SecretCollectionDisplay();

    @Getter
    public static final class SecretCollectionDisplay {
        private Boolean enabled = false;
        private ItemConfig item;
    }

    @Getter
    public static final class Items {
        private FillerItem filler;
        private ItemConfig previousPage;
        private ItemConfig currentPage;
        private ItemConfig nextPage;
        private ItemConfig back;
    }

    @Getter
    public static final class FillerItem {
        private Boolean enabled;
        private ItemConfig item;
    }

    @Getter
    public static final class CategoryIcon {
        private Boolean enabled;
        private ItemConfig item;
    }

    // Steps by config-version, only ever appended, each sets config-version itself
    public static List<Consumer<YamlConfiguration>> migrations() {
        return List.of(
                (yaml) -> {
                    yaml.set("category-icon.enabled", true);
                    yaml.set("category-icon.item.slot", 4);
                    yaml.set("config-version", 1);
                },
                (yaml) -> {
                    yaml.set("config-version", null);
                    yaml.set("secret-collection-display.enabled", false);
                    yaml.set("secret-collection-display.item.material", "gray_dye");
                    yaml.set("secret-collection-display.item.name", "&c{collection_name}");
                    yaml.set("secret-collection-display.item.lore", List.of("&7You haven't discovered this", "&7Collection yet!"));
                    yaml.set("config-version", 2);
                }
        );
    }
}
