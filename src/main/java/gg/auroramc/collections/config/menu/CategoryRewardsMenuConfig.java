package gg.auroramc.collections.config.menu;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class CategoryRewardsMenuConfig {
    private String title;
    private ProgressBar progressBar;
    private Map<String, ItemConfig> customItems;
    private Map<String, DisplayComponent> displayComponents;
    private List<Integer> displayArea;
    private Items items;
    private Integer rows = 6;

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
        private String filledCharacter;
        private String unfilledCharacter;
    }

    // Steps by config-version, only ever appended, each sets config-version itself
    public static List<Consumer<YamlConfiguration>> migrations() {
        return List.of(
                (yaml) -> {
                    yaml.set("config-version", null);
                    yaml.set("items.next-level", yaml.getConfigurationSection("items.locked-level"));
                    yaml.set("config-version", 1);
                }
        );
    }
}
