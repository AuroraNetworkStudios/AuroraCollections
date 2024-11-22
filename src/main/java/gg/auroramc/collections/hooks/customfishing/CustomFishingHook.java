package gg.auroramc.collections.hooks.customfishing;

import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.config.CollectionConfig;
import gg.auroramc.collections.hooks.Hook;
import gg.auroramc.collections.hooks.customfishing.listener.CustomFishingListener;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.loot.LootType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CustomFishingHook implements Hook {

    @Override
    public void hook(AuroraCollections plugin) {
        Bukkit.getPluginManager().registerEvents(new CustomFishingListener(plugin), plugin);
        if (!plugin.getConfigManager().getMetaConfig().isCustomFishingCollectionsSaved()) {
            generateDefaultCollections(plugin);
            plugin.getConfigManager().getMetaConfig().setCustomFishingCollectionsSaved(true);
            plugin.getConfigManager().getMetaConfig().saveChanges();
            AuroraCollections.logger().info("Generated default fishing collections for CustomFishing");
        }
        AuroraCollections.logger().info("Hooked into CustomFishing for fishing collection with namespace 'customfishing'");
    }

    private void generateDefaultCollections(AuroraCollections plugin) {
        for (var loot : BukkitCustomFishingPlugin.getInstance().getLootManager().getRegisteredLoots()) {
            if (loot.type() != LootType.ITEM) continue;
            if (Arrays.stream(loot.lootGroup()).noneMatch(s -> s.contains("river") || s.contains("ocean"))) continue;
            if (loot.id().equals("vanilla")) continue;

            var file = new File(plugin.getDataFolder(), "collections/fishing/0005_cf_" + loot.id() + ".yml");
            if (file.exists()) continue;

            var yaml = new YamlConfiguration();
            yaml.set("triggers", List.of("fish"));
            yaml.set("types", List.of("customfishing:" + loot.id()));
            yaml.set("name", loot.nick());

            yaml.set("menu-item.material", "customfishing:" + loot.id());

            yaml.set("requirements", List.of(50, 100, 250, 1000, 2500, 5000, 10000));
            yaml.set("use-global-level-matchers", true);

            try {
                file.createNewFile();
                yaml.save(file);
                var config = new CollectionConfig(file);
                config.load();
                plugin.getConfigManager().getCollections().get("fishing").put("0005_cf_" + loot.id(), config);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
