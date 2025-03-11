package gg.auroramc.collections.collection;

import com.google.common.collect.Maps;
import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.events.user.AuroraUserLoadedEvent;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.aurora.api.message.Chat;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.message.Text;
import gg.auroramc.aurora.api.reward.CommandReward;
import gg.auroramc.aurora.api.reward.ItemReward;
import gg.auroramc.aurora.api.reward.MoneyReward;
import gg.auroramc.aurora.api.reward.RewardAutoCorrector;
import gg.auroramc.aurora.api.reward.RewardExecutor;
import gg.auroramc.aurora.api.reward.RewardFactory;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.api.event.CollectionLevelUpEvent;
import gg.auroramc.collections.hooks.HookManager;
import gg.auroramc.collections.hooks.worldguard.WorldGuardHook;
import gg.auroramc.collections.listener.BlockBreakListener;
import gg.auroramc.collections.listener.DamageListener;
import gg.auroramc.collections.listener.EntityKillListener;
import gg.auroramc.collections.listener.FishingListener;
import gg.auroramc.collections.listener.HarvestingListener;
import gg.auroramc.collections.listener.PlayerKillListener;
import gg.auroramc.collections.listener.ShearListener;
import gg.auroramc.collections.listener.VillagerTradeListener;
import gg.auroramc.collections.reward.corrector.CommandCorrector;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CollectionManager implements Listener {
    private final AuroraCollections plugin;
    private final Map<String, Map<String, Collection>> categories = Maps.newConcurrentMap();
    @Getter
    private final RewardFactory rewardFactory = new RewardFactory();
    @Getter
    private final RewardAutoCorrector rewardAutoCorrector = new RewardAutoCorrector();
    private final Map<String, Category> categoryMap = Maps.newConcurrentMap();

    public CollectionManager(AuroraCollections plugin) {
        this.plugin = plugin;

        rewardFactory.registerRewardType(NamespacedId.fromDefault("command"), CommandReward.class);
        rewardFactory.registerRewardType(NamespacedId.fromDefault("money"), MoneyReward.class);
        rewardFactory.registerRewardType(NamespacedId.fromDefault("item"), ItemReward.class);

        rewardAutoCorrector.registerCorrector(NamespacedId.fromDefault("command"), new CommandCorrector(plugin));

        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(plugin), plugin);
        Bukkit.getPluginManager().registerEvents(new EntityKillListener(plugin), plugin);
        Bukkit.getPluginManager().registerEvents(new FishingListener(plugin), plugin);
        Bukkit.getPluginManager().registerEvents(new HarvestingListener(plugin), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerKillListener(plugin), plugin);
        Bukkit.getPluginManager().registerEvents(new ShearListener(plugin), plugin);
        Bukkit.getPluginManager().registerEvents(new DamageListener(plugin), plugin);
        Bukkit.getPluginManager().registerEvents(new VillagerTradeListener(plugin), plugin);
    }

    public java.util.Collection<Category> getCategories() {
        return categoryMap.values();
    }

    public List<Collection> getAllCollections() {
        return categories.values().stream().flatMap(map -> map.values().stream()).toList();
    }

    public Category getCategory(String category) {
        return categoryMap.get(category);
    }

    public java.util.Collection<Collection> getCollectionsForCategory(String category) {
        return categories.get(category).values();
    }

    public int getMaxCategoryLevel(String category) {
        if (!categories.containsKey(category)) return 0;
        return categories.get(category).values().stream().mapToInt(Collection::getMaxLevel).sum();
    }

    public int getCategoryLevel(String category, Player player) {
        if (!categories.containsKey(category)) return 0;
        return categories.get(category).values().stream().mapToInt(c -> c.getPlayerLevel(player)).sum();
    }

    public double getCategoryCompletionPercent(String category, Player player) {
        return getCategoryLevel(category, player) / Math.max(getMaxCategoryLevel(category), 1D);
    }

    public List<Collection> getCollectionsByCategory(String category) {
        return List.copyOf(categories.getOrDefault(category, Map.of()).values());
    }

    public Collection getCollection(String category, String name) {
        var collectionMap = categories.get(category);
        if (collectionMap != null) {
            return collectionMap.get(name);
        }
        return null;
    }

    public boolean hasCategory(String category) {
        return categories.containsKey(category);
    }

    public void progressCollections(Player player, TypeId type, int amount, String... triggers) {
        if (!player.hasPermission("aurora.collections.use")) return;
        if (plugin.getConfigManager().getConfig().getPreventCreativeMode() && player.getGameMode() == GameMode.CREATIVE)
            return;
        if (!AuroraAPI.getUserManager().getUser(player).isLoaded()) return;

        if (HookManager.isEnabled(WorldGuardHook.class)) {
            if (HookManager.getHook(WorldGuardHook.class).isBlocked(player, player.getLocation())) return;
        }

        CompletableFuture.runAsync(() -> {
            var toUpdate = new HashSet<String>();

            for (var category : categories.entrySet()) {
                if (!getCategory(category.getKey()).hasPermission(player)) continue;

                for (var collection : category.getValue().values()) {
                    if (!collection.hasPermission(player)) continue;

                    String firstMatch = Arrays.stream(triggers)
                            .filter(trigger -> collection.getConfig().getParsedTriggers().contains(trigger))
                            .findFirst()
                            .orElse(null);

                    if (firstMatch != null) {
                        collection.progress(player, type, amount, firstMatch);
                        toUpdate.add(collection.getCategory() + "_" + collection.getId());
                        toUpdate.add("cc_" + collection.getCategory());
                    }
                }
            }

            if (!toUpdate.isEmpty()) {
                var user = AuroraAPI.getUserManager().getUser(player);
                if (!user.isLoaded()) return;
                AuroraAPI.getLeaderboards().updateUser(user, toUpdate.toArray(new String[0]));
            }
        });
    }

    public void reloadCollections() {
        categoryMap.clear();
        categories.clear();
        var config = plugin.getConfigManager().getCollections();
        for (var category : config.entrySet()) {
            var categoryMap = Maps.<String, Collection>newLinkedHashMap();
            for (var collection : category.getValue().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
                categoryMap.put(collection.getKey(), new Collection(plugin, collection.getValue(), category.getKey(), collection.getKey()));
            }
            categories.put(category.getKey(), categoryMap);
        }

        for (var entry : plugin.getConfigManager().getCategoriesConfig().getCategories().entrySet()) {
            categoryMap.put(entry.getKey(), new Category(entry.getKey(), rewardFactory, entry.getValue()));
            if (!categories.containsKey(entry.getKey())) {
                categories.put(entry.getKey(), Maps.newConcurrentMap());
            }
        }
    }

    @EventHandler
    public void onUserLoaded(AuroraUserLoadedEvent e) {
        CompletableFuture.runAsync(() -> rewardAutoCorrector.correctRewards(e.getUser().getPlayer()));
    }

    @EventHandler
    public void onCollectionLevelUp(CollectionLevelUpEvent e) {
        var categoryId = e.getCollection().getCategory();
        var category = categoryMap.get(categoryId);

        if (!category.isLevelingEnabled()) return;

        var player = e.getPlayer();

        int level = getCategoryLevel(categoryId, player);
        var rewards = category.getRewards(level - 1, level, getMaxCategoryLevel(categoryId));

        if (rewards.isEmpty()) return;

        double highestPercent = 0;

        var currentPercent = getCategoryCompletionPercent(categoryId, player) * 100;
        for (var r : category.getRewards()) {
            if (r.percentage() > highestPercent && currentPercent >= r.percentage()) {
                highestPercent = r.percentage();
            }
        }

        List<Placeholder<?>> placeholders = List.of(
                Placeholder.of("{player}", player.getName()),
                Placeholder.of("{category_name}", categoryMap.get(categoryId).getConfig().getName()),
                Placeholder.of("{category_id}", categoryId),
                Placeholder.of("{percent}", AuroraAPI.formatNumber(highestPercent))
        );

        var lvlUpMsg = plugin.getConfigManager().getConfig().getCategoryLevelUpMessage();

        int count = 0;
        if (lvlUpMsg.getEnabled()) {
            var text = Component.text();
            var messageLines = lvlUpMsg.getMessage();
            var mainConfig = plugin.getConfigManager().getConfig();

            for (var line : messageLines) {
                count++;
                if (line.equals("component:rewards")) {

                    if (!rewards.isEmpty()) {
                        text.append(Text.component(e.getPlayer(), mainConfig.getDisplayComponents().get("rewards").getTitle(), placeholders));
                    }
                    for (var reward : rewards) {
                        text.append(Component.newline());
                        var display = mainConfig.getDisplayComponents().get("rewards").getLine().replace("{reward}", reward.getDisplay(player, placeholders));
                        text.append(Text.component(player, display, placeholders));
                    }
                } else {
                    text.append(Text.component(player, line, placeholders));
                }

                if (count != messageLines.size()) text.append(Component.newline());
            }

            if (lvlUpMsg.getOpenMenuWhenClicked()) {
                text.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/" + mainConfig.getCommandAliases().getCollections().get(0) + " " +
                                mainConfig.getCommandAliases().getProgression().get(0) + " " +
                                category.getId()));
            }

            Chat.sendMessage(player, text.build());
        }

        RewardExecutor.execute(rewards, player, level, placeholders);
    }

    public List<Placeholder<?>> getCategoryPlaceholders(String category, Player player) {
        List<Placeholder<?>> placeholders = new ArrayList<>(13);

        var categoryName = plugin.getConfigManager().getCategoriesConfig().getCategories().get(category).getName();
        var percentRaw = this.getCategoryCompletionPercent(category, player);
        var currentPercentage = AuroraAPI.formatNumber(percentRaw * 100);

        var boardName = "cc_" + category;
        var lb = AuroraAPI.getUser(player.getUniqueId()).getLeaderboardEntries().get(boardName);
        var lbm = AuroraAPI.getLeaderboards();

        if (lb != null && lb.getPosition() != 0) {
            placeholders.add(Placeholder.of("{lb_position}", AuroraAPI.formatNumber(lb.getPosition())));
            placeholders.add(Placeholder.of("{lb_position_percent}", AuroraAPI.formatNumber(
                    Math.min(((double) lb.getPosition() / Math.max(1, AuroraAPI.getLeaderboards().getBoardSize(boardName))) * 100, 100)
            )));
            placeholders.add(Placeholder.of("{lb_size}",
                    AuroraAPI.formatNumber(
                            Math.max(Math.max(lb.getPosition(), Bukkit.getOnlinePlayers().size()), AuroraAPI.getLeaderboards().getBoardSize(boardName)))));
        } else {
            placeholders.add(Placeholder.of("{lb_position}", lbm.getEmptyPlaceholder()));
            placeholders.add(Placeholder.of("{lb_position_percent}", lbm.getEmptyPlaceholder()));
            placeholders.add(Placeholder.of("{lb_size}",
                    AuroraAPI.formatNumber(Math.max(Bukkit.getOnlinePlayers().size(), AuroraAPI.getLeaderboards().getBoardSize(boardName)))));
        }

        var totalCollected = this.getCollectionsByCategory(category).stream()
                .mapToLong(collection -> collection.getCount(player)).sum();

        placeholders.add(Placeholder.of("{total_formatted}", AuroraAPI.formatNumber(totalCollected)));
        placeholders.add(Placeholder.of("{total}", totalCollected));
        placeholders.add(Placeholder.of("{total_short}", AuroraAPI.formatNumberShort(totalCollected)));

        var collectionsInCategory = plugin.getCollectionManager().getCollectionsByCategory(category);
        var maxedCollections = collectionsInCategory.stream().filter(c -> c.isMaxed(player)).count();
        var unlockedCollections = collectionsInCategory.stream().filter(c -> c.isUnlocked(player)).count();
        var maxedPercent = Math.min((double) maxedCollections / collectionsInCategory.size(), 1);
        var unlockedPercent = Math.min((double) unlockedCollections / collectionsInCategory.size(), 1);

        var config = plugin.getConfigManager().getCategoriesMenuConfig();
        var bar = config.getProgressBar();
        var pcs = bar.getLength();

        var completedPercent = Math.min(percentRaw, 1);
        var completedPcs = ((Double) Math.floor(pcs * completedPercent)).intValue();
        var remainingPcs = pcs - completedPcs;

        var maxedCompletedPercent = Math.min(maxedPercent, 1);
        var maxedCompletedPcs = ((Double) Math.floor(pcs * maxedCompletedPercent)).intValue();
        var maxedRemainingPcs = pcs - maxedCompletedPcs;

        var unlockedCompletedPcs = ((Double) Math.floor(pcs * unlockedPercent)).intValue();
        var unlockedRemainingPcs = pcs - unlockedCompletedPcs;

        placeholders.add(Placeholder.of("{name}", categoryName));
        placeholders.add(Placeholder.of("{progress_percent}", currentPercentage));
        placeholders.add(Placeholder.of("{progressbar}", bar.getFilledCharacter().repeat(completedPcs) + bar.getUnfilledCharacter().repeat(remainingPcs) + "&r"));
        placeholders.add(Placeholder.of("{maxed_progressbar}", bar.getFilledCharacter().repeat(maxedCompletedPcs) + bar.getUnfilledCharacter().repeat(maxedRemainingPcs) + "&r"));
        placeholders.add(Placeholder.of("{maxed_collection_count}", AuroraAPI.formatNumber(maxedCollections)));
        placeholders.add(Placeholder.of("{total_collection_count}", AuroraAPI.formatNumber(collectionsInCategory.size())));
        placeholders.add(Placeholder.of("{maxed_progress_percent}", AuroraAPI.formatNumber(maxedPercent * 100)));
        placeholders.add(Placeholder.of("{unlocked_collection_count}", AuroraAPI.formatNumber(unlockedCollections)));
        placeholders.add(Placeholder.of("{unlocked_progressbar}", bar.getFilledCharacter().repeat(unlockedCompletedPcs) + bar.getUnfilledCharacter().repeat(unlockedRemainingPcs) + "&r"));
        placeholders.add(Placeholder.of("{unlocked_progress_percent}", AuroraAPI.formatNumber(unlockedPercent * 100)));

        return placeholders;
    }

    public List<Placeholder<?>> getGlobalPlaceholders(Player player) {
        List<Placeholder<?>> placeholders = new ArrayList<>();

        var allCollections = plugin.getCollectionManager().getAllCollections();
        var unlockedCollections = allCollections.stream().filter(c -> c.isUnlocked(player)).count();
        var totalCollections = allCollections.size();

        var unlockedPercent = totalCollections > 0 ? (double) unlockedCollections / totalCollections : 0;

        var config = plugin.getConfigManager().getCategoriesMenuConfig();
        var bar = config.getProgressBar();
        var pcs = bar.getLength();
        var completedPcs = (int) Math.floor(pcs * unlockedPercent);
        var remainingPcs = pcs - completedPcs;

        placeholders.add(Placeholder.of("{unlocked_total_count}", AuroraAPI.formatNumber(unlockedCollections)));
        placeholders.add(Placeholder.of("{total_all_collection_count}", AuroraAPI.formatNumber(totalCollections)));
        placeholders.add(Placeholder.of("{unlocked_total_progressbar}", bar.getFilledCharacter().repeat(completedPcs) + bar.getUnfilledCharacter().repeat(remainingPcs) + "&r"));
        placeholders.add(Placeholder.of("{unlocked_total_percent}", AuroraAPI.formatNumber(unlockedPercent * 100)));

        return placeholders;
    }
}
