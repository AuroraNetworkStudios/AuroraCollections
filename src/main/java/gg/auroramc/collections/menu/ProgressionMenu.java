package gg.auroramc.collections.menu;

import gg.auroramc.aurora.api.levels.ConcreteMatcher;
import gg.auroramc.aurora.api.levels.IntervalMatcher;
import gg.auroramc.aurora.api.menu.AuroraMenu;
import gg.auroramc.aurora.api.menu.ItemBuilder;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.message.Text;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.collection.Collection;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ProgressionMenu {
    @Getter
    private final static NamespacedId menuId = NamespacedId.fromDefault("collections_progression_menu");

    private final Player player;
    private final AuroraCollections plugin;
    private final Collection collection;
    private int page = 0;

    public ProgressionMenu(Player player, AuroraCollections plugin, Collection collection) {
        this.player = player;
        this.plugin = plugin;
        this.collection = collection;
    }

    public void open() {
        createMenu().open();
    }

    private AuroraMenu createMenu() {
        var config = plugin.getConfigManager().getCollectionMenuConfig();

        var menu = new AuroraMenu(player, config.getTitle(), config.getRows() * 9, false, menuId,
                Placeholder.of("{collection_name}", collection.getConfig().getName()),
                Placeholder.of("{collection_title}", collection.getMenuTitle())
        );

        if (config.getItems().getFiller().getEnabled()) {
            menu.addFiller(ItemBuilder.of(config.getItems().getFiller().getItem()).toItemStack(player));
        } else {
            menu.addFiller(ItemBuilder.filler(Material.AIR));
        }

        var currentPlaceholders = collection.getPlaceholders(player, collection.getPlayerLevel(player));

        for (var customItem : config.getCustomItems().values()) {
            menu.addItem(ItemBuilder.of(customItem).placeholder(currentPlaceholders).build(player));
        }

        for (var customItem : collection.getConfig().getCustomMenuItems().values()) {
            menu.addItem(ItemBuilder.of(customItem).placeholder(currentPlaceholders).build(player));
        }

        var items = config.getItems();

        menu.addItem(ItemBuilder.back(items.getBack()).build(player), (e) -> {
            new CollectionsMenu(player, plugin, collection.getCategory()).open();
        });

        var template = config.getCollectionMenuTemplate();
        var iconConfig = template.getEnabled() ? template.getItem().merge(collection.getConfig().getMenuItem()) : collection.getConfig().getMenuItem();

        var iconBuilder = ItemBuilder.of(iconConfig)
                .placeholder(collection.getPlaceholders(player, collection.getPlayerLevel(player) + 1))
                .defaultSlot(4);

        menu.addItem(iconBuilder.build(player));

        var requirements = getPage(page, config.getDisplayArea().size());

        for (int i = 0; i < config.getDisplayArea().size(); i++) {
            var slot = config.getDisplayArea().get(i);
            if (requirements.size() <= i) {
                break;
            }

            var requirement = requirements.get(i);
            var level = collection.getConfig().getRequirements().indexOf(requirement) + 1;
            var playerLevel = collection.getPlayerLevel(player);
            var completed = playerLevel >= level;
            var inProgress = playerLevel + 1 == level;
            var matcher = collection.getLevelMatcher().getBestMatcher(level);
            var itemConfig = inProgress ? config.getItems().getNextLevel() : completed ? config.getItems().getCompletedLevel() : config.getItems().getLockedLevel();

            var mergeKey = inProgress ? "next-level" : completed ? "completed-level" : "locked-level";

            if (matcher instanceof IntervalMatcher intervalMatcher) {
                if (intervalMatcher.getConfig().getItem().containsKey("generic-level")) {
                    itemConfig = itemConfig.merge(intervalMatcher.getConfig().getItem().get("generic-level"));
                }
                itemConfig = itemConfig.merge(intervalMatcher.getConfig().getItem().get(mergeKey));
            } else if (matcher instanceof ConcreteMatcher concreteMatcher) {
                if (concreteMatcher.getConfig().getItem().containsKey("generic-level")) {
                    itemConfig = itemConfig.merge(concreteMatcher.getConfig().getItem().get("generic-level"));
                }
                itemConfig = itemConfig.merge(concreteMatcher.getConfig().getItem().get(mergeKey));
            }

            var placeholders = collection.getPlaceholders(player, level);
            var lore = new ArrayList<String>();
            var rewards = matcher.computeRewards(level);

            for (var line : itemConfig.getLore()) {
                if (line.equals("component:rewards")) {
                    var display = config.getDisplayComponents().get("rewards");

                    if (!rewards.isEmpty()) {
                        lore.add(display.getTitle());
                    }
                    for (var reward : rewards) {
                        lore.add(display.getLine().replace("{reward}", reward.getDisplay(player, placeholders)));
                    }
                } else {
                    lore.add(line);
                }
            }

            var builder = ItemBuilder.of(itemConfig).slot(slot)
                    .loreCompute(() -> lore.stream().map(l -> Text.component(player, l, placeholders)).toList())
                    .placeholder(placeholders);

            if (config.getAllowItemAmounts()) {
                builder.amount(level);
            }

            menu.addItem(builder.build(player));
        }


        var pageCount = getTotalPageCount(config.getDisplayArea().size());

        if (collection.getConfig().getRequirements().size() > config.getDisplayArea().size()) {
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

    private List<Integer> getPage(int page, int pageSize) {
        var requirements = collection.getConfig().getRequirements();
        return requirements.stream().skip((long) page * pageSize).limit(pageSize).toList();
    }

    private int getTotalPageCount(int pageSize) {
        var requirements = collection.getConfig().getRequirements();
        return (int) Math.ceil((double) requirements.size() / pageSize) - 1;
    }
}
