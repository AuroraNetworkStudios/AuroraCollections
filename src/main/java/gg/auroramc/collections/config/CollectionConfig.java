package gg.auroramc.collections.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.decorators.IgnoreField;
import gg.auroramc.aurora.api.config.premade.ConcreteMatcherConfig;
import gg.auroramc.aurora.api.config.premade.IntervalMatcherConfig;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.aurora.api.item.TypeId;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class CollectionConfig extends AuroraConfig {
    private Set<String> triggers;
    private Set<String> types;
    private String name;
    private String menuTitle;
    private String permission;
    private List<Integer> requirements;
    private Boolean useGlobalLevelMatchers;
    private Map<String, IntervalMatcherConfig> levelMatchers;
    private Map<String, ConcreteMatcherConfig> customLevels;
    private ItemConfig menuItem;
    private Map<String, ItemConfig> customMenuItems;
    private List<Multiplier> multipliers = new ArrayList<>();

    @IgnoreField
    private Set<TypeId> parsedTypes;

    @IgnoreField
    private Set<String> parsedTriggers;

    @IgnoreField
    private List<ParsedMultiplier> parsedMultipliers;

    @Getter
    public static final class CustomLevel {
        private ConfigurationSection rewards;
    }

    @Getter
    public static final class Multiplier {
        private List<String> triggers = new ArrayList<>();
        private List<String> types = new ArrayList<>();
        private Integer value = 1;
    }

    @Getter
    public static final class ParsedMultiplier {
        private final Set<String> triggers;
        private final Set<TypeId> types;
        private final Integer value;

        public ParsedMultiplier(Multiplier multiplier) {
            this.triggers = multiplier.getTriggers().stream().map(String::toUpperCase).collect(Collectors.toSet());
            this.types = multiplier.getTypes().stream().map(TypeId::fromDefault).collect(Collectors.toSet());
            this.value = multiplier.getValue();
        }
    }

    public CollectionConfig(File file) {
        super(file);
    }

    @Override
    public void load() {
        super.load();
        parsedTypes = types.stream().map(TypeId::fromDefault).collect(Collectors.toSet());
        parsedTriggers = triggers.stream().map(String::toUpperCase).collect(Collectors.toSet());
        parsedMultipliers = multipliers.stream().map(ParsedMultiplier::new).toList();
    }
}
