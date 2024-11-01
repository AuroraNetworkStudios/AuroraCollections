package gg.auroramc.collections;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.AuroraLogger;
import gg.auroramc.aurora.api.item.ItemManager;
import gg.auroramc.collections.api.AuroraCollectionsProvider;
import gg.auroramc.collections.api.data.CollectionData;
import gg.auroramc.collections.collection.CollectionManager;
import gg.auroramc.collections.command.CommandManager;
import gg.auroramc.collections.config.ConfigManager;
import gg.auroramc.collections.hooks.HookManager;
import gg.auroramc.collections.placeholder.CollectionsPlaceholderHandler;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public final class AuroraCollections extends JavaPlugin {

    @Getter
    private ConfigManager configManager;
    private CommandManager commandManager;
    @Getter
    private CollectionManager collectionManager;

    @Getter
    private ItemManager itemManager;

    private static AuroraLogger l;

    public static AuroraLogger logger() {
        return l;
    }

    @Override
    public void onLoad() {
        configManager = new ConfigManager(this);
        l = AuroraAPI.createLogger("AuroraCollections", () -> configManager.getConfig().getDebug());
        configManager.reload();
        HookManager.loadHooks(this);

        for (var entry : configManager.getCollections().entrySet()) {
            AuroraAPI.getLeaderboards().registerBoard(
                    "cc_" + entry.getKey(),
                    (user) -> collectionManager.getCollectionsByCategory(entry.getKey()).stream().mapToDouble((collection) -> user.getData(CollectionData.class).getCollectionCount(collection.getCategory(), collection.getId())).sum(),
                    (lb) -> AuroraAPI.formatNumberShort(((Double) lb.getValue()).longValue()),
                    configManager.getConfig().getLeaderboard().getCacheSize(),
                    configManager.getConfig().getLeaderboard().getMinItemsCollected().doubleValue()
            );

            for (var id : entry.getValue().keySet()) {
                AuroraAPI.getLeaderboards().registerBoard(
                        entry.getKey() + "_" + id,
                        (user) -> Double.valueOf(user.getData(CollectionData.class).getCollectionCount(entry.getKey(), id)),
                        (lb) -> AuroraAPI.formatNumber(((Double) lb.getValue()).longValue()),
                        configManager.getConfig().getLeaderboard().getCacheSize(),
                        configManager.getConfig().getLeaderboard().getMinItemsCollected().doubleValue()
                );
            }
        }
    }

    @Override
    public void onEnable() {
        itemManager = AuroraAPI.getItemManager();

        AuroraAPI.getUserManager().registerUserDataHolder(CollectionData.class);
        AuroraAPI.registerPlaceholderHandler(new CollectionsPlaceholderHandler(this));

        commandManager = new CommandManager(this);
        commandManager.reload();

        collectionManager = new CollectionManager(this);

        HookManager.enableHooks(this);

        Bukkit.getGlobalRegionScheduler().run(this, (task) -> collectionManager.reloadCollections());

        try {
            var field = AuroraCollectionsProvider.class.getDeclaredField("plugin");
            field.setAccessible(true);
            field.set(null, this);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            l.severe("Failed to initialize api provider! error: " + e.getMessage());
        }

        new Metrics(this, 23778);
    }

    public void reload() {
        configManager.reload();
        commandManager.reload();
        collectionManager.reloadCollections();

        CompletableFuture.runAsync(() ->
                Bukkit.getOnlinePlayers().forEach(player ->
                        collectionManager.getRewardAutoCorrector().correctRewards(player)));

    }

    @Override
    public void onDisable() {
        commandManager.unregisterCommands();
    }
}
