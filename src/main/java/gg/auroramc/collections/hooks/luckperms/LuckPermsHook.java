package gg.auroramc.collections.hooks.luckperms;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.aurora.api.reward.PermissionReward;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.collections.AuroraCollections;

public class LuckPermsHook implements Hook<AuroraCollections> {
    @Override
    public void hook(AuroraCollections plugin) {
        plugin.getCollectionManager().getRewardFactory()
                .registerRewardType(NamespacedId.fromDefault("permission"), PermissionReward.class);

        plugin.getCollectionManager().getRewardAutoCorrector()
                .registerCorrector(NamespacedId.fromDefault("permission"), new PermissionCorrector(plugin));

        AuroraCollections.logger().info("Hooked into LuckPerms for permission rewards with reward type: 'permission'. Auto reward corrector for permissions is registered.");
    }
}
