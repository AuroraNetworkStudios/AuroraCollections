package gg.auroramc.collections.menu;

import gg.auroramc.aurora.api.menu.AuroraMenu;
import gg.auroramc.aurora.api.menu.ItemBuilder;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.collections.AuroraCollections;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class CategoryMenu {
    @Getter
    private final static NamespacedId menuId = NamespacedId.fromDefault("collections_category_menu");

    private final Player player;
    private final AuroraCollections plugin;

    public CategoryMenu(Player player, AuroraCollections plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    public void open() {
        createMenu().open();
    }

    private AuroraMenu createMenu() {
        var config = plugin.getConfigManager().getCategoriesMenuConfig();

        var menu = new AuroraMenu(player, config.getTitle(), config.getRows() * 9, false, menuId);

        if (config.getFiller().getEnabled()) {
            menu.addFiller(ItemBuilder.of(config.getFiller().getItem()).toItemStack(player));
        } else {
            menu.addFiller(ItemBuilder.filler(Material.AIR));
        }

        var globalPlaceholders = plugin.getCollectionManager().getGlobalPlaceholders(player);

        for (var customItem : config.getCustomItems().values()) {
            menu.addItem(ItemBuilder.of(customItem).placeholder(globalPlaceholders).build(player));
        }

        for (var item : config.getItems().entrySet()) {
            var category = item.getKey();
            var placeholders = plugin.getCollectionManager().getCategoryPlaceholders(category, player);
            placeholders.addAll(globalPlaceholders);
            var builtItem = ItemBuilder.of(item.getValue()).placeholder(placeholders).build(player);

            if (plugin.getCollectionManager().getCategory(category).hasPermission(player)) {
                menu.addItem(builtItem);
            } else {
                menu.addItem(builtItem, (e) -> {
                    if (e.isRightClick() && plugin.getCollectionManager().getCategory(category).isLevelingEnabled()) {
                        new CategoryRewardsMenu(player, plugin, category).open();
                    } else {
                        new CollectionsMenu(player, plugin, category).open();
                    }
                });
            }

        }

        return menu;
    }
}
