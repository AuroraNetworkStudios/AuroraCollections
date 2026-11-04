package gg.auroramc.collections;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.AuroraLogger;
import gg.auroramc.aurora.api.item.ItemManager;
import gg.auroramc.aurora.api.plugin.Hooks;
import gg.auroramc.collections.api.AuroraCollectionsProvider;
import gg.auroramc.collections.api.data.CollectionData;
import gg.auroramc.collections.collection.CollectionManager;
import gg.auroramc.collections.command.CommandManager;
import gg.auroramc.collections.config.ConfigManager;
import gg.auroramc.collections.hooks.auraskills.AuraSkillsHook;
import gg.auroramc.collections.hooks.auroralevels.AuroraLevelsHook;
import gg.auroramc.collections.hooks.beeminions.BeeMinionsHook;
import gg.auroramc.collections.hooks.beeminions.BeeMinionsReworkHook;
import gg.auroramc.collections.hooks.customfishing.CustomFishingHook;
import gg.auroramc.collections.hooks.luckperms.LuckPermsHook;
import gg.auroramc.collections.hooks.mmocore.MMOCoreHook;
import gg.auroramc.collections.hooks.mmoitems.MMOItemsHook;
import gg.auroramc.collections.hooks.mmolib.MMOLibHook;
import gg.auroramc.collections.hooks.mythic.MythicHook;
import gg.auroramc.collections.hooks.nexo.NexoHook;
import gg.auroramc.collections.hooks.oraxen.OraxenHook;
import gg.auroramc.collections.hooks.pyrofishing.PyroFishingHook;
import gg.auroramc.collections.hooks.topminions.TopMinionsHook;
import gg.auroramc.collections.hooks.worldguard.WorldGuardHook;
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
    // Created in onLoad, after checking that Aurora is new enough (an older one doesn't have Hooks)
    @Getter
    private Hooks<AuroraCollections> hooks;
    @Getter
    private CollectionManager collectionManager;

    @Getter
    private ItemManager itemManager;

    private static AuroraLogger l;

    public static AuroraLogger logger() {
        return l;
    }

    // Set when Aurora is too old for this version, nothing else is done then
    private boolean incompatible = false;

    @Override
    public void onLoad() {
        if (!auroraIsCompatible()) {
            incompatible = true;
            return;
        }
        configManager = new ConfigManager(this);
        l = AuroraAPI.createLogger("AuroraCollections", () -> configManager.getConfig().getDebug());
        configManager.reload();
        hooks = new Hooks<>(this)
                .add(AuroraLevelsHook.class, "AuroraLevels")
                .add(AuraSkillsHook.class, "AuraSkills")
                .add(LuckPermsHook.class, "LuckPerms")
                .add(CustomFishingHook.class, "CustomFishing")
                .add(MMOItemsHook.class, "MMOItems")
                .add(MythicHook.class, "MythicMobs")
                .add(OraxenHook.class, "Oraxen")
                .add(NexoHook.class, "Nexo")
                .add(WorldGuardHook.class, "WorldGuard")
                .add(MMOLibHook.class, "MythicLib")
                .add(TopMinionsHook.class, "TopMinion")
                .add(BeeMinionsHook.class, "BeeMinions")
                .add(BeeMinionsReworkHook.class, "BeeMinionsRework")
                .add(MMOCoreHook.class, "MMOCore")
                .add(PyroFishingHook.class, "PyroFishingPro");
        hooks.load();

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
        if (incompatible) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        itemManager = AuroraAPI.getItemManager();

        AuroraAPI.getUserManager().registerUserDataHolder(CollectionData.class);
        AuroraAPI.registerPlaceholderHandler(new CollectionsPlaceholderHandler(this));

        commandManager = new CommandManager(this);
        commandManager.reload();

        collectionManager = new CollectionManager(this);

        hooks.enable();

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
        if (commandManager != null) commandManager.unregisterCommands();
    }

    // Built for Aurora 3: with an older one, say so clearly instead of failing somewhere deep inside. Only Bukkit is
    // used here, an older Aurora doesn't have what this version needs.
    private boolean auroraIsCompatible() {
        var aurora = Bukkit.getPluginManager().getPlugin("Aurora");
        var version = aurora == null ? "none" : aurora.getPluginMeta().getVersion();
        int major;
        try {
            major = Integer.parseInt(version.split("[.-]")[0]);
        } catch (NumberFormatException e) {
            major = 0;
        }
        if (major >= 3) return true;
        getLogger().severe(getName() + " " + getPluginMeta().getVersion() + " needs Aurora 3.0 or newer, but Aurora " + version + " is installed. Update Aurora.");
        return false;
    }
}
