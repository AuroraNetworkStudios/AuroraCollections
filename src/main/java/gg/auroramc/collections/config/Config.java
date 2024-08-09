package gg.auroramc.collections.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.premade.IntervalMatcherConfig;
import gg.auroramc.collections.AuroraCollections;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class Config extends AuroraConfig {
    private Boolean debug = false;
    private String language = "en";
    private CommandAliasConfig commandAliases;
    private Map<String, IntervalMatcherConfig> globalLevelMatchers;
    private LevelUpSound levelUpSound;
    private LevelUpMessage levelUpMessage;
    private LevelUpMessage categoryLevelUpMessage;
    private Map<String, DisplayComponent> displayComponents;
    private Boolean preventCreativeMode = false;
    private LeaderboardConfig leaderboard;

    public Config(AuroraCollections plugin) {
        super(getFile(plugin));
    }

    @Getter
    public static final class LeaderboardConfig {
        private Integer cacheSize = 10;
        private Integer minItemsCollected = 10;
    }

    @Getter
    public static final class CommandAliasConfig {
        private List<String> collections = List.of("collections");
    }

    @Getter
    public static final class DisplayComponent {
        private String title;
        private String line;
    }

    @Getter
    public static final class LevelUpMessage {
        private Boolean enabled;
        private List<String> message;
    }

    @Getter
    public static final class LevelUpSound {
        private Boolean enabled;
        private String sound;
        private Float volume;
        private Float pitch;
    }

    public static File getFile(AuroraCollections plugin) {
        return new File(plugin.getDataFolder(), "config.yml");
    }

    public static void saveDefault(AuroraCollections plugin) {
        if (!getFile(plugin).exists()) {
            plugin.saveResource("config.yml", false);
        }
    }

    @Override
    protected List<Consumer<YamlConfiguration>> getMigrationSteps() {
        return List.of(
                (yaml) -> {
                    yaml.set("config-version", null);

                    yaml.set("category-level-up-message.enabled", true);
                    yaml.set("category-level-up-message.message", List.of(
                            "&3&m----------------------------------------&r",
                            " ",
                            "  &f&l{category_name} milestone reached &6&l{percent}%&r",
                            " ",
                            "component:rewards",
                            " ",
                            "&3&m----------------------------------------"
                    ));

                    yaml.set("config-version", 1);
                }
        );
    }
}
