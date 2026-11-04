package gg.auroramc.collections.hooks.mmolib;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.aurora.api.reward.integration.MMOStatReward;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.collections.AuroraCollections;

public class MMOLibHook implements Hook<AuroraCollections> {
    // Collections' stat modifiers in MythicLib, kept apart from other plugins'
    public static final String KEY = "aurora_collections";
    @Override
    public void hook(AuroraCollections plugin) {
        plugin.getCollectionManager().getRewardFactory()
                .registerRewardType(NamespacedId.fromDefault("mmo_stat"), () -> new MMOStatReward(KEY, plugin, AuroraCollections.logger()));

        plugin.getCollectionManager().getRewardAutoCorrector()
                .registerCorrector(NamespacedId.fromDefault("mmo_stat"), new MMOStatCorrector(plugin));
    }
}
