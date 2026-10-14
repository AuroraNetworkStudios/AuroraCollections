package gg.auroramc.collections.config.menu;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.collections.AuroraCollections;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class CollectionMenuConfig extends AuroraConfig {
    private String title;
    private ProgressBar progressBar;
    private Map<String, ItemConfig> customItems;
    private Map<String, DisplayComponent> displayComponents;
    private List<Integer> displayArea;
    private Items items;
    private Boolean allowItemAmounts = false;
    private Boolean forceRomanNumerals = false;
    private ItemTemplate collectionMenuTemplate;
    private Integer rows = 6;
    private Boolean categoryIcon = false;

    public CollectionMenuConfig(AuroraCollections plugin) {
        super(getFile(plugin));
    }

    @Getter
    public static final class ItemTemplate {
        private ItemConfig item;
        private Boolean enabled = true;
    }

    @Getter
    public static final class DisplayComponent {
        private String title;
        private String line;
    }

    @Getter
    public static final class Items {
        private FillerItem filler;
        private ItemConfig previousPage;
        private ItemConfig currentPage;
        private ItemConfig nextPage;
        private ItemConfig completedLevel;
        private ItemConfig lockedLevel;
        private ItemConfig nextLevel;
        private ItemConfig back;
    }

    @Getter
    public static final class FillerItem {
        private Boolean enabled;
        private ItemConfig item;
    }

    @Getter
    public static final class ProgressBar {
        private Integer length = 20;
        private String filledCharacter = "";
        private String unfilledCharacter = "";
    }

    public static File getFile(AuroraCollections plugin) {
        return new File(plugin.getDataFolder() + "/menus", "collection.yml");
    }

    public static void saveDefault(AuroraCollections plugin) {
        if (!getFile(plugin).exists()) {
            plugin.saveResource("menus/collection.yml", false);
        }
    }

    @Override
    protected List<Consumer<YamlConfiguration>> getMigrationSteps() {
        return List.of(
                (yaml) -> {
                    yaml.set("config-version", null);
                    yaml.set("items.next-level", yaml.getConfigurationSection("items.locked-level"));
                    yaml.set("config-version", 1);
                }
        );
    }
}
