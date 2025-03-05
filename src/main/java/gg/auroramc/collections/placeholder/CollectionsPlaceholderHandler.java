package gg.auroramc.collections.placeholder;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.placeholder.PlaceholderHandler;
import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.api.data.CollectionData;
import gg.auroramc.collections.collection.Collection;
import gg.auroramc.collections.util.RomanNumber;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class CollectionsPlaceholderHandler implements PlaceholderHandler {
    private final AuroraCollections plugin;

    public record Pair(String category, String id) {
    }

    public CollectionsPlaceholderHandler(AuroraCollections plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "collections";
    }

    @Override
    public String onPlaceholderRequest(Player player, String[] args) {
        if (args.length < 3) return null;

        var manager = plugin.getCollectionManager();
        var config = plugin.getConfigManager();

        var allCollections = manager.getAllCollections();
        var unlockedCollections = allCollections.stream().filter(c -> c.isUnlocked(player)).count();
        var totalCollections = allCollections.size();
        var unlockedPercent = totalCollections > 0 ? (double) unlockedCollections / totalCollections : 0;
        var bar = config.getCategoriesMenuConfig().getProgressBar();
        var pcs = bar.getLength();
        var completedPcs = (int) Math.floor(pcs * unlockedPercent);
        var remainingPcs = pcs - completedPcs;

        var romanNumeral = config.getCollectionMenuConfig().getForceRomanNumerals();
        var full = String.join("_", args);

        if (args[0].equals("category")) {
            if (full.endsWith("completed_percent_raw")) {
                var category = String.join("_", Arrays.copyOfRange(args, 1, args.length - 3));
                return String.valueOf(manager.getCategoryCompletionPercent(category, player));
            } else if (full.endsWith("completed_percent")) {
                var category = String.join("_", Arrays.copyOfRange(args, 1, args.length - 2));
                return AuroraAPI.formatNumber(manager.getCategoryCompletionPercent(category, player) * 100);
            } else if (full.endsWith("level_raw")) {
                var category = String.join("_", Arrays.copyOfRange(args, 1, args.length - 2));
                return String.valueOf(manager.getCategoryLevel(category, player));
            } else if (full.endsWith("max_level")) {
                var category = String.join("_", Arrays.copyOfRange(args, 1, args.length - 2));
                var maxLevel = manager.getMaxCategoryLevel(category);
                return romanNumeral ? RomanNumber.toRoman((long) maxLevel) : AuroraAPI.formatNumber(maxLevel);
            } else if (full.endsWith("level")) {
                var category = String.join("_", Arrays.copyOfRange(args, 1, args.length - 1));
                var level = manager.getCategoryLevel(category, player);
                return romanNumeral ? RomanNumber.toRoman((long) level) : AuroraAPI.formatNumber(level);
            } else if (full.endsWith("count_raw")) {
                return String.valueOf(getCategoryCount(player, args, 2));
            } else if (full.endsWith("count_short")) {
                return AuroraAPI.formatNumberShort(getCategoryCount(player, args, 2));
            } else if (full.endsWith("count")) {
                return AuroraAPI.formatNumber(getCategoryCount(player, args, 1));
            }
        } else if (full.endsWith("next_count_raw")) {
            var collection = getCollection(Arrays.copyOf(args, args.length - 3));
            if (collection == null) return null;
            var nextCount = collection.getRequiredAmount(collection.getPlayerLevel(player) + 1);
            return String.valueOf(nextCount);
        } else if (full.endsWith("next_count")) {
            var collection = getCollection(Arrays.copyOf(args, args.length - 2));
            if (collection == null) return null;
            var nextCount = collection.getRequiredAmount(collection.getPlayerLevel(player) + 1);
            return AuroraAPI.formatNumber(nextCount);
        } else if (full.endsWith("count_raw")) {
            var collection = getCollection(Arrays.copyOf(args, args.length - 2));
            if (collection == null) return null;
            var count = AuroraAPI.getUserManager().getUser(player).getData(CollectionData.class).getCollectionCount(collection.getCategory(), collection.getId());
            return String.valueOf(count);
        } else if (full.endsWith("count")) {
            var collection = getCollection(Arrays.copyOf(args, args.length - 1));
            if (collection == null) return null;
            var count = AuroraAPI.getUserManager().getUser(player).getData(CollectionData.class).getCollectionCount(collection.getCategory(), collection.getId());
            return AuroraAPI.formatNumber(count);
        } else if (full.endsWith("level_raw")) {
            var collection = getCollection(Arrays.copyOf(args, args.length - 2));
            if (collection == null) return null;
            return String.valueOf(collection.getPlayerLevel(player));
        } else if (full.endsWith("level")) {
            var collection = getCollection(Arrays.copyOf(args, args.length - 1));
            if (collection == null) return null;
            var level = collection.getPlayerLevel(player);
            return romanNumeral ? RomanNumber.toRoman((long) level) : AuroraAPI.formatNumber(level);
        } else if (full.endsWith("name")) {
            var collection = getCollection(Arrays.copyOf(args, args.length - 1));
            if (collection == null) return null;
            return collection.getConfig().getName();
        } else if (args[0].equals("unlocked")) {
            if (full.endsWith("count_total")) {
                return AuroraAPI.formatNumber(unlockedCollections);
            } else if (full.endsWith("count_total_all_collection")) {
                return AuroraAPI.formatNumber(totalCollections);
            } else if (full.endsWith("total_progressbar")) {
                return bar.getFilledCharacter().repeat(completedPcs) + bar.getUnfilledCharacter().repeat(remainingPcs) + "&r";
            } else if (full.endsWith("total_percent")) {
                return AuroraAPI.formatNumber(unlockedPercent * 100);
            }
        }

        return null;
    }

    private double getCategoryCount(Player player, String[] args, int length) {
        var category = String.join("_", Arrays.copyOfRange(args, 1, args.length - length));
        var user = AuroraAPI.getUserManager().getUser(player);
        return plugin.getCollectionManager().getCollectionsByCategory(category).stream()
                .mapToDouble((collection) -> user.getData(CollectionData.class).getCollectionCount(collection.getCategory(), collection.getId())).sum();
    }

    public Collection getCollection(String[] args) {
        var manager = plugin.getCollectionManager();
        if (manager.getCollection(args[0], args[1]) != null) return manager.getCollection(args[0], args[1]);

        for (int i = 0; i < args.length; i++) {
            var category = String.join("_", Arrays.copyOf(args, i));
            var id = String.join("_", Arrays.copyOfRange(args, i, args.length));
            if (manager.getCollection(category, id) != null) return manager.getCollection(category, id);
        }

        return null;
    }

    @Override
    public List<String> getPatterns() {
        var manager = plugin.getCollectionManager();

        return Stream.concat(
                manager.getAllCollections().stream().flatMap(c -> Stream.of(
                        "category_" + c.getCategory() + "_level",
                        "category_" + c.getCategory() + "_level_raw",
                        "category_" + c.getCategory() + "_max_level",
                        "category_" + c.getCategory() + "_completed_percent",
                        "category_" + c.getCategory() + "_completed_percent_raw",
                        c.getCategory() + "_" + c.getId() + "_name",
                        c.getCategory() + "_" + c.getId() + "_level",
                        c.getCategory() + "_" + c.getId() + "_level_raw",
                        c.getCategory() + "_" + c.getId() + "_count",
                        c.getCategory() + "_" + c.getId() + "_count_raw",
                        c.getCategory() + "_" + c.getId() + "_next_count",
                        c.getCategory() + "_" + c.getId() + "_next_count_raw"
                )),
                Stream.of(
                        "unlocked_count_total",
                        "unlocked_count_total_all_collection",
                        "unlocked_total_progressbar",
                        "unlocked_total_percent"
                )
        ).toList();
    }
}
