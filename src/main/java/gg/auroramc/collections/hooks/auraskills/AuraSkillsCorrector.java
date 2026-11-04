package gg.auroramc.collections.hooks.auraskills;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.reward.RewardCorrector;
import gg.auroramc.aurora.api.reward.integration.AuraSkillsStatReward;
import gg.auroramc.collections.AuroraCollections;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AuraSkillsCorrector implements RewardCorrector {
    private final AuroraCollections plugin;
    private final Set<UUID> toLoad = Sets.newConcurrentHashSet();

    public AuraSkillsCorrector(AuroraCollections plugin) {
        this.plugin = plugin;
    }

    @Override
    public void correctRewards(Player player) {
        if (AuraSkillsApi.get().getUser(player.getUniqueId()).isLoaded()) {
            correctRewardsWhenLoaded(player, true);
        } else {
            toLoad.add(player.getUniqueId());
        }
    }

    public void correctRewardsWhenLoaded(Player player, boolean force) {
        if (!force && !toLoad.contains(player.getUniqueId())) return;
        toLoad.remove(player.getUniqueId());

        var manager = plugin.getCollectionManager();
        Map<Stat, Double> statMap = Maps.newHashMap();

        // Reset all stat modifiers first
        for (var stat : AuraSkillsApi.get().getGlobalRegistry().getStats()) {
            statMap.put(stat, 0.0);
        }

        // Gather new stat modifiers
        for (var collection : manager.getAllCollections()) {
            var level = collection.getPlayerLevel(player);

            for (int i = 1; i < level + 1; i++) {
                var matcher = collection.getLevelMatcher().getBestMatcher(i);
                if (matcher == null) continue;
                var placeholders = collection.getPlaceholders(player, i);
                for (var reward : matcher.computeRewards(i)) {
                    if (reward instanceof AuraSkillsStatReward statReward) {
                        statMap.merge(statReward.getStat(), statReward.getValue(placeholders), Double::sum);
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
                if (reward instanceof AuraSkillsStatReward statReward) {
                    statMap.merge(statReward.getStat(), statReward.getValue(placeholders), Double::sum);
                }
            }
        }

        player.getScheduler().run(plugin, (task) -> {
            for (var entry : statMap.entrySet()) {
                var statKey = AuraSkillsHook.PREFIX + entry.getKey().getId().toString();
                var user = AuraSkillsApi.get().getUser(player.getUniqueId());

                var oldModifier = user.getStatModifier(statKey);

                if (oldModifier == null) {
                    if (entry.getValue() > 0) {
                        user.addStatModifier(new StatModifier(statKey, entry.getKey(), entry.getValue()));
                    }
                } else if (entry.getValue() <= 0) {
                    user.removeStatModifier(statKey);
                } else if (entry.getValue() != oldModifier.value()) {
                    user.addStatModifier(new StatModifier(statKey, entry.getKey(), entry.getValue()));
                }
            }
        }, null);
    }
}
