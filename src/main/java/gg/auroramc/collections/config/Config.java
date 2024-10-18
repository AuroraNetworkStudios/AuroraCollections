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
    private GenericSound levelUpSound;
    private GenericSound discoverSound;
    private GenericMessage levelUpMessage;
    private GenericMessage discoverMessage;
    private GenericMessage categoryLevelUpMessage;
    private Map<String, DisplayComponent> displayComponents;
    private LeaderboardConfig leaderboard;
    private Boolean preventCreativeMode = false;

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
        private List<String> progression = List.of("progression");
    }

    @Getter
    public static final class DisplayComponent {
        private String title;
        private String line;
    }

    @Getter
    public static final class GenericMessage {
        private Boolean enabled;
        private Boolean openMenuWhenClicked = false;
        private List<String> message;
    }

    @Getter
    public static final class GenericSound {
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
                },
                (yaml) -> {
                    yaml.set("config-version", null);

                    yaml.set("level-up-message.open-menu-when-clicked", true);
                    yaml.set("category-level-up-message.open-menu-when-clicked", true);
                    yaml.set("command-aliases.progression", List.of("progression"));

                    yaml.set("config-version", 2);
                },
                (yaml) -> {
                    yaml.set("config-version", null);

                    yaml.set("discover-message.enabled", false);
                    yaml.set("discover-message.open-menu-when-clicked", true);
                    yaml.set("discover-message.message", List.of(
                            "&3&m----------------------------------------&r",
                            ' ',
                            "  &f&l{collection_name} collection discovered&r",
                            ' ',
                            "&3&m----------------------------------------"
                    ));

                    yaml.set("discover-sound.enabled", false);
                    yaml.set("discover-sound.sound", "ENTITY_PLAYER_LEVELUP");
                    yaml.set("discover-sound.volume", 1);
                    yaml.set("discover-sound.pitch", 1);

                    yaml.set("config-version", 3);
                }
        );
    }
}
