package gg.auroramc.collections.hooks.auraskills;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.aurora.api.reward.integration.AuraSkillsStatReward;
import gg.auroramc.aurora.api.reward.integration.AuraSkillsXpReward;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.collections.AuroraCollections;
import lombok.Getter;
import org.bukkit.Bukkit;

@Getter
public class AuraSkillsHook implements Hook<AuroraCollections> {
    // Collections' stat modifiers in AuraSkills start with this, kept apart from other plugins'
    public static final String PREFIX = "AURORA_COLLECTIONS_";

    private AuraSkillsCorrector corrector;

    @Override
    public void hook(AuroraCollections plugin) {
        this.corrector = new AuraSkillsCorrector(plugin);

        plugin.getCollectionManager().getRewardFactory()
                .registerRewardType(NamespacedId.fromDefault("auraskills_stat"), () -> new AuraSkillsStatReward(PREFIX, AuroraCollections.logger()));
        plugin.getCollectionManager().getRewardFactory()
                .registerRewardType(NamespacedId.fromDefault("auraskills_xp"), () -> new AuraSkillsXpReward(AuroraCollections.logger()));

        plugin.getCollectionManager().getRewardAutoCorrector()
                .registerCorrector(NamespacedId.fromDefault("auraskills_stat"), corrector);

        Bukkit.getPluginManager().registerEvents(new AuraSkillsListener(plugin, this), plugin);

        AuroraCollections.logger().info("Hooked into AuraSkills for handling extra loot drops");
        AuroraCollections.logger().info("Hooked into AuraSkills for stat rewards with reward type: 'auraskills_stat' and 'auraskills_xp'. Auto reward corrector for stats is registered.");
    }
}
