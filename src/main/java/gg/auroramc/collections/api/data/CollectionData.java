package gg.auroramc.collections.api.data;

import com.google.common.collect.Maps;
import gg.auroramc.aurora.api.user.UserDataHolder;
import gg.auroramc.aurora.api.util.NamespacedId;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Getter
public class CollectionData extends UserDataHolder {
    private final Map<String, Map<String, Long>> cache = Maps.newConcurrentMap();

    @Override
    public NamespacedId getId() {
        return NamespacedId.fromDefault("collections");
    }

    @Override
    public void serializeInto(ConfigurationSection data) {
        data.getKeys(false).forEach(key -> data.set(key, null));

        for (var entry : cache.entrySet()) {
            var section = data.createSection(entry.getKey());
            for (var innerEntry : entry.getValue().entrySet()) {
                section.set(innerEntry.getKey(), innerEntry.getValue());
            }
        }
    }

    @Override
    public void initFrom(@Nullable ConfigurationSection data) {
        if (data == null) return;

        for (var key : data.getKeys(false)) {
            var section = data.getConfigurationSection(key);
            if (section == null) continue;

            Map<String, Long> innerMap = Maps.newConcurrentMap();

            for (var innerKey : section.getKeys(false)) {
                innerMap.put(innerKey, section.getLong(innerKey));
            }

            cache.put(key, innerMap);
        }
    }

    public Long getCollectionCount(String category, String collection) {
        return cache.getOrDefault(category, Maps.newConcurrentMap()).getOrDefault(collection, 0L);
    }

    public void incrementCollectionCount(String category, String collection, int amount, int maxRequirement) {
        cache.computeIfAbsent(category, k -> Maps.newConcurrentMap())
                .compute(collection, (k, v) -> v == null ? Math.min(amount, maxRequirement) : Math.min(v + amount, maxRequirement));
        // After the change, see UserDataHolder#dirty
        dirty.set(true);
    }

    public void incrementCollectionCount(String category, String collection, int amount) {
        cache.computeIfAbsent(category, k -> Maps.newConcurrentMap())
                .compute(collection, (k, v) -> v == null ? amount : v + amount);
        dirty.set(true);
    }

    public void setDirty() {
        dirty.set(true);
    }
}
