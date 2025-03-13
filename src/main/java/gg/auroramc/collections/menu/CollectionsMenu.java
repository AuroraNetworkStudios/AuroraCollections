package gg.auroramc.collections.menu;

import gg.auroramc.aurora.api.menu.AuroraMenu;
import gg.auroramc.aurora.api.menu.ItemBuilder;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.collection.Collection;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CollectionsMenu {
    @Getter
    private final static NamespacedId menuId = NamespacedId.fromDefault("collections_menu");

    private final Player player;
    private final AuroraCollections plugin;
    private final String category;
    private int page = 0;

    public CollectionsMenu(Player player, AuroraCollections plugin, String category) {
        this.player = player;
        this.plugin = plugin;
        this.category = category;
    }

    public void open() {
        createMenu().open();
    }

    private AuroraMenu createMenu() {
        var config = plugin.getConfigManager().getCollectionListMenuConfig();
        var cConfig = plugin.getConfigManager().getCollectionMenuConfig();
        var categories = plugin.getConfigManager().getCategoriesConfig().getCategories();

        var menu = new AuroraMenu(player, config.getTitle(), config.getRows() * 9, false, menuId, Placeholder.of("{category}", categories.get(category).getName()));

        if (config.getItems().getFiller().getEnabled()) {
            menu.addFiller(ItemBuilder.of(config.getItems().getFiller().getItem()).toItemStack(player));
        } else {
            menu.addFiller(ItemBuilder.filler(Material.AIR));
        }

        for (var customItem : config.getCustomItems().values()) {
            menu.addItem(ItemBuilder.of(customItem).build(player));
        }

        var items = config.getItems();

        menu.addItem(ItemBuilder.back(items.getBack()).build(player), (e) -> {
            new CategoryMenu(player, plugin).open();
        });


        // Category icon
        if (plugin.getConfigManager().getCollectionListMenuConfig().getCategoryIcon().getEnabled()) {
            var itemConfig = plugin.getConfigManager().getCategoriesMenuConfig().getItems().get(category)
                    .merge(plugin.getConfigManager().getCollectionListMenuConfig().getCategoryIcon().getItem());

            var placeholders = plugin.getCollectionManager().getCategoryPlaceholders(category, player);
            menu.addItem(ItemBuilder.of(itemConfig).placeholder(placeholders).build(player));
        }


        var collections = getPage(page, config.getDisplayArea().size());

        for (int i = 0; i < config.getDisplayArea().size(); i++) {
            var slot = config.getDisplayArea().get(i);
            if (collections.size() <= i) {
                menu.addItem(ItemBuilder.item(new ItemStack(Material.AIR)).slot(slot).build(player));
                continue;
            }

            var collection = collections.get(i);
            var template = cConfig.getCollectionMenuTemplate();

            var itemConfig = template.getEnabled() ? template.getItem().merge(collection.getConfig().getMenuItem()) : collection.getConfig().getMenuItem();

            boolean hidden = false;
            if (config.getSecretCollectionDisplay().getEnabled()) {
                if (collection.getCount(player) == 0) {
                    itemConfig = config.getSecretCollectionDisplay().getItem();
                    hidden = true;
                }
            }

            var builder = ItemBuilder.of(itemConfig).slot(slot)
                    .placeholder(collection.getPlaceholders(player, collection.getPlayerLevel(player) + 1));

            if (hidden) {
                menu.addItem(builder.build(player));
            } else {
                menu.addItem(builder.build(player), (e) -> {
                    if (!collection.hasPermission(player)) return;
                    new ProgressionMenu(player, plugin, collection).open();
                });
            }
        }

        var pageCount = getTotalPageCount(config.getDisplayArea().size());

        if (plugin.getCollectionManager().getCollectionsByCategory(category).size() > config.getDisplayArea().size()) {
            List<Placeholder<?>> placeholders = List.of(Placeholder.of("{current}", page + 1), Placeholder.of("{max}", pageCount + 1));

            menu.addItem(ItemBuilder.of(items.getPreviousPage()).defaultSlot(48).placeholder(placeholders).build(player),
                    (e) -> {
                        if (page > 0) {
                            page--;
                            createMenu().open();
                        }
                    });

            menu.addItem(ItemBuilder.of(items.getCurrentPage()).defaultSlot(49)
                    .placeholder(placeholders).build(player));

            menu.addItem(ItemBuilder.of(items.getNextPage()).defaultSlot(50).placeholder(placeholders).build(player),
                    (e) -> {
                        if (page < pageCount) {
                            page++;
                            createMenu().open();
                        }
                    });

        }

        return menu;
    }

    private List<Collection> getPage(int page, int pageSize) {
        var collections = plugin.getCollectionManager().getCollectionsByCategory(category);
        return collections.stream().skip((long) page * pageSize).limit(pageSize).toList();
    }

    private int getTotalPageCount(int pageSize) {
        var collections = plugin.getCollectionManager().getCollectionsByCategory(category);
        return (int) Math.ceil((double) collections.size() / pageSize) - 1;
    }
}
