package gg.auroramc.collections.hooks.mythic.reward;

import com.google.common.collect.Maps;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.reward.RewardCorrector;
import gg.auroramc.aurora.api.reward.integration.MythicStatReward;
import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.hooks.mythic.MythicHook;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.stats.StatModifierType;
import io.lumine.mythic.core.skills.stats.StatType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class MythicStatCorrector implements RewardCorrector {
    private final AuroraCollections plugin;

    public MythicStatCorrector(AuroraCollections plugin) {
        this.plugin = plugin;
    }

    @Override
    public void correctRewards(Player player) {
        var manager = plugin.getCollectionManager();
        var mythic = MythicBukkit.inst();
        var registry = mythic.getPlayerManager().getProfile(player).getStatRegistry();

        Map<StatType, Map<StatModifierType, Double>> statMap = Maps.newHashMap();

        mythic.getStatManager().getStats().values()
                .forEach(statType -> {
                    if (!statType.isEnabled()) return;
                    registry.removeValue(statType, MythicHook.SOURCE);
                });

        // Gather new stat modifiers
        for (var collection : manager.getAllCollections()) {
            var level = collection.getPlayerLevel(player);

            for (int i = 1; i < level + 1; i++) {
                var matcher = collection.getLevelMatcher().getBestMatcher(i);
                if (matcher == null) continue;
                var placeholders = collection.getPlaceholders(player, i);
                for (var reward : matcher.computeRewards(i)) {
                    if (reward instanceof MythicStatReward statReward && statReward.isValid()) {
                        statMap.computeIfAbsent(statReward.getStatType(), (key) -> Maps.newHashMap())
                                .merge(statReward.getModifierType(), statReward.getValue(placeholders), Double::sum);
                    }
                }
            }
        }

        for (var category : manager.getCategories()) {
            if (!category.isLevelingEnabled()) continue;
            var rewards = category.getRewards(manager.getCategoryLevel(category.getId(), player), manager.getMaxCategoryLevel(category.getId()));

            List<Placeholder<?>> placeholders = List.of(
                    Placeholder.of("{player}", player.getName()),
                    Placeholder.of("{category_name}", category.getConfig().getName()),
                    Placeholder.of("{category_id}", category.getId())
            );

            for (var reward : rewards) {
                if (reward instanceof MythicStatReward statReward && statReward.isValid()) {
                    statMap.computeIfAbsent(statReward.getStatType(), (key) -> Maps.newHashMap())
                            .merge(statReward.getModifierType(), statReward.getValue(placeholders), Double::sum);
                }
            }
        }

        // Apply the new stat modifiers
        for (var entry : statMap.entrySet()) {
            var statType = entry.getKey();
            for (var modifierEntry : entry.getValue().entrySet()) {
                var modifierType = modifierEntry.getKey();
                var value = modifierEntry.getValue();
                AuroraCollections.logger().debug("Adding stat " + statType.getKey() + " with value " + value + " to player " + player.getName());
                registry.putValue(statType, MythicHook.SOURCE, modifierType, value);
            }
        }
    }
}
