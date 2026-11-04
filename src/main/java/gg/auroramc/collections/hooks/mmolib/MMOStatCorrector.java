package gg.auroramc.collections.hooks.mmolib;

import com.google.common.collect.Maps;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.reward.RewardCorrector;
import gg.auroramc.aurora.api.reward.integration.MMOStatReward;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.collections.AuroraCollections;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MMOStatCorrector implements RewardCorrector {
    private final AuroraCollections plugin;

    public MMOStatCorrector(AuroraCollections plugin) {
        this.plugin = plugin;
    }

    @Override
    public void correctRewards(Player player) {
        var manager = plugin.getCollectionManager();

        MMOPlayerData playerData = MMOPlayerData.get(player);
        StatMap stats = playerData.getStatMap();

        Map<String, MMOStat> statMap = Maps.newHashMap();

        // Gather new stat modifiers
        for (var collection : manager.getAllCollections()) {
            var level = collection.getPlayerLevel(player);

            for (int i = 1; i < level + 1; i++) {
                var matcher = collection.getLevelMatcher().getBestMatcher(i);
                if (matcher == null) continue;
                var placeholders = collection.getPlaceholders(player, i);
                for (var reward : matcher.computeRewards(i)) {
                    if (reward instanceof MMOStatReward statReward && statReward.isValid()) {
                        var key = NamespacedId.of(MMOLibHook.KEY, statReward.getStat()).toString();
                        var current = statReward.getCurrentModifier(key, stats);
                        UUID uuid = current != null ? current.getUniqueId() : UUID.randomUUID();
                        statMap.merge(statReward.getStat(),
                                new MMOStat(statReward.getModifierType(), statReward.getValue(placeholders), key, uuid),
                                (a, b) -> new MMOStat(statReward.getModifierType(), a.value() + b.value(), a.key(), a.uuid()));
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
                if (reward instanceof MMOStatReward statReward && statReward.isValid()) {
                    var key = NamespacedId.of(MMOLibHook.KEY, statReward.getStat()).toString();
                    var current = statReward.getCurrentModifier(key, stats);
                    UUID uuid = current != null ? current.getUniqueId() : UUID.randomUUID();
                    statMap.merge(statReward.getStat(),
                            new MMOStat(statReward.getModifierType(), statReward.getValue(placeholders), key, uuid),
                            (a, b) -> new MMOStat(statReward.getModifierType(), a.value() + b.value(), a.key(), a.uuid()));
                }
            }
        }

        player.getScheduler().runDelayed(plugin, (task) -> {
            for (var entry : statMap.entrySet()) {
                var statType = entry.getKey();
                var s = entry.getValue();

                new StatModifier(s.uuid, s.key, statType, s.value, s.modifierType, EquipmentSlot.OTHER, ModifierSource.OTHER)
                        .register(playerData);
            }
        }, null, 3);
    }

    public record MMOStat(ModifierType modifierType, double value, String key, UUID uuid) {
    }
}
