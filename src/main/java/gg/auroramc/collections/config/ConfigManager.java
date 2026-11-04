package gg.auroramc.collections.config;

import com.google.common.collect.Maps;
import gg.auroramc.aurora.api.config.ConfigFile;
import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.config.menu.CategoriesMenuConfig;
import gg.auroramc.collections.config.menu.CategoryRewardsMenuConfig;
import gg.auroramc.collections.config.menu.CollectionListMenuConfig;
import gg.auroramc.collections.config.menu.CollectionMenuConfig;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ConfigManager {
    private final AuroraCollections plugin;
    private ConfigFile<Config> config;
    private ConfigFile<MetaConfig> metaConfig;
    // By language: only the configured language is used, a file stays loaded so a reload can keep it if it's broken
    private final Map<String, ConfigFile<MessageConfig>> messageConfigs = new ConcurrentHashMap<>();
    private volatile String language;
    private ConfigFile<CategoriesConfig> categoriesConfig;
    private ConfigFile<CategoriesMenuConfig> categoriesMenuConfig;
    private ConfigFile<CollectionListMenuConfig> collectionListMenuConfig;
    private ConfigFile<CollectionMenuConfig> collectionMenuConfig;
    private ConfigFile<CategoryRewardsMenuConfig> categoryRewardsMenuConfig;
    @Getter
    private final Map<String, Map<String, CollectionConfig>> collections = Maps.newConcurrentMap();

    public ConfigManager(AuroraCollections plugin) {
        this.plugin = plugin;
    }

    @SneakyThrows
    public void reload() {
        if (config == null) {
            boolean saveDefaultCollections = !new File(plugin.getDataFolder(), "config.yml").exists();
            config = ConfigFile.builder(plugin, "config.yml", Config.class).migrations(Config.migrations()).build();
            metaConfig = ConfigFile.builder(plugin, "meta.yml", MetaConfig.class).build();
            categoriesConfig = ConfigFile.builder(plugin, "categories.yml", CategoriesConfig.class).migrations(CategoriesConfig.migrations()).build();
            categoriesMenuConfig = ConfigFile.builder(plugin, "menus/categories.yml", CategoriesMenuConfig.class).migrations(CategoriesMenuConfig.migrations()).build();
            collectionListMenuConfig = ConfigFile.builder(plugin, "menus/collection_list.yml", CollectionListMenuConfig.class).migrations(CollectionListMenuConfig.migrations()).build();
            collectionMenuConfig = ConfigFile.builder(plugin, "menus/collection.yml", CollectionMenuConfig.class).migrations(CollectionMenuConfig.migrations()).build();
            categoryRewardsMenuConfig = ConfigFile.builder(plugin, "menus/category_rewards.yml", CategoryRewardsMenuConfig.class).migrations(CategoryRewardsMenuConfig.migrations()).build();
            for (var file : List.of(config, metaConfig, categoriesConfig, categoriesMenuConfig, collectionListMenuConfig, collectionMenuConfig, categoryRewardsMenuConfig)) {
                file.get();
            }
            loadMessages(false);
            if (saveDefaultCollections) {
                this.saveDefaultCollections();
            }
        } else {
            for (var file : List.of(config, metaConfig, categoriesConfig, categoriesMenuConfig, collectionListMenuConfig, collectionMenuConfig, categoryRewardsMenuConfig)) {
                file.reload();
            }
            loadMessages(true);
        }

        reloadCollections();
    }

    private void loadMessages(boolean reload) {
        var language = getConfig().getLanguage();
        var existing = messageConfigs.get(language);
        if (existing != null) {
            if (reload) existing.reload();
        } else {
            var name = "messages_" + language + ".yml";
            if (!hasResource(name) && !new File(plugin.getDataFolder(), name).exists()) {
                AuroraCollections.logger().warning("Internal message file for language: " + language + " not found! Creating a new one from english...");
            }
            messageConfigs.put(language, ConfigFile.builder(plugin, name, MessageConfig.class)
                    .bundled(hasResource(name) ? name : "messages_en.yml")
                    .migrations(MessageConfig.migrations())
                    .build());
            messageConfigs.get(language).get();
        }
        this.language = language;
    }

    private boolean hasResource(String name) {
        try (var in = plugin.getResource(name)) {
            return in != null;
        } catch (IOException e) {
            return false;
        }
    }

    public Config getConfig() {
        return config.get();
    }

    public MetaConfig getMetaConfig() {
        return metaConfig.get();
    }

    // Only that line of meta.yml changes
    public void setCustomFishingCollectionsSaved() {
        metaConfig.set("custom-fishing-collections-saved", true);
    }

    public MessageConfig getMessageConfig() {
        return messageConfigs.get(language).get();
    }

    public CategoriesConfig getCategoriesConfig() {
        return categoriesConfig.get();
    }

    public CategoriesMenuConfig getCategoriesMenuConfig() {
        return categoriesMenuConfig.get();
    }

    public CollectionListMenuConfig getCollectionListMenuConfig() {
        return collectionListMenuConfig.get();
    }

    public CollectionMenuConfig getCollectionMenuConfig() {
        return collectionMenuConfig.get();
    }

    public CategoryRewardsMenuConfig getCategoryRewardsMenuConfig() {
        return categoryRewardsMenuConfig.get();
    }

    private void reloadCollections() {
        collections.clear();
        Path collectionsDir = plugin.getDataFolder().toPath().resolve("collections");

        if (!Files.exists(collectionsDir) || !Files.isDirectory(collectionsDir)) {
            return;
        }

        for (var dir : collectionsDir.toFile().listFiles()) {
            if (!dir.isDirectory()) {
                AuroraCollections.logger().warning("File " + dir.getName() + " is in the collections directory, but should be in a category subdirectory");
            } else if (!getCategoriesConfig().getCategories().keySet().contains(dir.getName())) {
                AuroraCollections.logger().warning("Category " + dir.getName() + " does not exist in categories.yml");
            }
        }

        var validIdPattern = Pattern.compile("^[a-zA-Z0-9_-]+$");

        for (var dir : getCategoriesConfig().getCategories().keySet()) {
            Path categoryDir = collectionsDir.resolve(dir);
            if (!Files.exists(categoryDir)) {
                continue;
            }

            if (!validIdPattern.matcher(dir).matches()) {
                AuroraCollections.logger().severe("Category ID: '" + dir + "' doesn't match the required format ^[a-zA-Z0-9_-]+$. " +
                        "Use only english alphabet, numbers, underscores and hyphens. " +
                        "Category will be loaded, but you may experience issues.");
            }

            try (Stream<Path> paths = Files.list(categoryDir)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".yml"))
                        .forEach(path -> {
                            var fileName = path.getFileName().toString().replace(".yml", "");

                            if (fileName.contains(".")) {
                                AuroraCollections.logger().severe("ID: '" + fileName + "' contains a dot/period (.), which is not allowed. Skipping.");
                                return;
                            }

                            if (!validIdPattern.matcher(fileName).matches()) {
                                AuroraCollections.logger().warning("ID: '" + fileName + "' doesn't match the required format ^[a-zA-Z0-9_-]+$. " +
                                        "Use only english alphabet, numbers, underscores and hyphens. " +
                                        "Collection will be loaded, but you may experience issues.");
                            }

                            CollectionConfig collectionConfig = CollectionConfig.load(path.toFile());
                            collections.computeIfAbsent(dir, k -> Maps.newConcurrentMap()).computeIfAbsent(fileName, k -> collectionConfig);
                        });
            } catch (IOException e) {
                AuroraCollections.logger().warning("Failed to load collections for category: " + dir + " error: " + e.getMessage());
            }
        }
    }

    @SneakyThrows
    private void saveDefaultCollections() {
        Path jarPath = Path.of(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
        Path dataFolder = plugin.getDataFolder().toPath();

        try (JarFile jar = new JarFile(jarPath.toFile())) {
            jar.stream()
                    .filter(entry -> entry.getName().startsWith("collections/") && entry.getName().endsWith(".yml"))

                    .forEach(entry -> {
                        Path outFile = dataFolder.resolve(entry.getName());
                        Path parentDir = outFile.getParent();
                        if (parentDir != null && !Files.exists(parentDir)) {
                            try {
                                Files.createDirectories(parentDir);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        try (InputStream is = jar.getInputStream(entry)) {
                            Files.copy(is, outFile, StandardCopyOption.REPLACE_EXISTING);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
