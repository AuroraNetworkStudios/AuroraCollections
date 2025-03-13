package gg.auroramc.collections.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.message.Chat;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.collection.Collection;
import gg.auroramc.collections.menu.CategoryMenu;
import gg.auroramc.collections.menu.CategoryRewardsMenu;
import gg.auroramc.collections.menu.CollectionsMenu;
import gg.auroramc.collections.menu.ProgressionMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandAlias("%collectionsAlias")
public class CollectionsCommand extends BaseCommand {
    private final AuroraCollections plugin;

    public CollectionsCommand(AuroraCollections plugin) {
        this.plugin = plugin;
    }

    @Default
    @Description("Opens the collections menu")
    @CommandPermission("aurora.collections.use")
    public void onMenu(Player player) {
        if (!AuroraAPI.getUser(player.getUniqueId()).isLoaded()) {
            Chat.sendMessage(player, plugin.getConfigManager().getMessageConfig().getDataNotLoadedYetSelf());
            return;
        }
        new CategoryMenu(player, plugin).open();
    }

    @Subcommand("reload")
    @Description("Reloads the plugin configs and applies reward auto correctors to players")
    @CommandPermission("aurora.collections.admin.reload")
    public void onReload(CommandSender sender) {
        plugin.reload();
        Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getReloaded());
    }

    @Subcommand("open")
    @Description("Opens the collections menu for another player in a specific category")
    @CommandCompletion("@players @categories|none true|false")
    @CommandPermission("aurora.collections.admin.open")
    public void onOpenMenu(CommandSender sender, @Flags("other") Player target, @Default("none") String category, @Default("false") Boolean silent) {
        if (category.equals("none")) {
            new CategoryMenu(target, plugin).open();
        } else {
            new CollectionsMenu(target, plugin, category).open();
        }

        if (!silent) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getMenuOpened(), Placeholder.of("{player}", target.getName()));
        }
    }

    @Subcommand("%progressionAlias")
    @Description("Opens the collection progression menu for another player")
    @CommandCompletion("@categories @collections|none")
    @CommandPermission("aurora.collections.use.open")
    public void onOpenProgressionMenu(Player sender, String category, @Default("none") String collectionId) {
        if (collectionId.equals("none")) {
            if (!plugin.getCollectionManager().hasCategory(category)) return;
            if (plugin.getCollectionManager().getCategory(category).isLevelingEnabled()) {
                if (!plugin.getCollectionManager().getCategory(category).hasPermission(sender)) {
                    Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getNoPermission());
                    return;
                }
                new CategoryRewardsMenu(sender, plugin, category).open();
            }
        } else {
            var collection = plugin.getCollectionManager().getCollection(category, collectionId);
            if (collection == null) return;
            if (!collection.hasPermission(sender)) {
                Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getNoPermission());
                return;
            }

            new ProgressionMenu(sender, plugin, collection).open();
        }
    }

    @Subcommand("add")
    @Description("Adds progress to a player's collection")
    @CommandCompletion("@players @categories @collections|all @range:1-1000 true|false")
    @CommandPermission("aurora.collections.admin.add")
    public void onAdd(CommandSender sender, @Flags("other") Player target, String category, String collectionId, Integer number, @Default("false") Boolean silent) {
        var collections = getCollection(sender, category, collectionId);
        if (collections == null) return;

        var validCollections = collections.stream().filter(c -> c.hasPermission(target)).toList();

        for (var collection : validCollections) {
            collection.progress(target, null, number, null);
        }

        if (!silent) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getAddSuccess(),
                    Placeholder.of("{player}", target.getName()),
                    Placeholder.of("{number}", AuroraAPI.formatNumber(number)),
                    Placeholder.of("{collection}", String.join(", ", validCollections.stream().map(Collection::getId).toList()))
            );
        }
    }

    @Subcommand("remove")
    @Description("Removes progress from a player's collection")
    @CommandCompletion("@players @categories @collections|all @range:1-1000 true|false")
    @CommandPermission("aurora.collections.admin.remove")
    public void onRemove(CommandSender sender, @Flags("other") Player target, String category, String collectionId, Integer number, @Default("false") Boolean silent) {
        var collections = getCollection(sender, category, collectionId);
        if (collections == null) return;

        for (var collection : collections) {
            collection.removeProgress(target, number);
        }

        if (!silent) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getRemoveSuccess(),
                    Placeholder.of("{player}", target.getName()),
                    Placeholder.of("{number}", AuroraAPI.formatNumber(number)),
                    Placeholder.of("{collection}", String.join(", ", collections.stream().map(Collection::getId).toList()))
            );
        }
    }

    @Subcommand("set")
    @Description("Sets progress of a player's collection")
    @CommandCompletion("@players @categories @collections|all @range:0-1000 true|false")
    @CommandPermission("aurora.collections.admin.set")
    public void onSet(CommandSender sender, @Flags("other") Player target, String category, String collectionId, Integer number, @Default("false") Boolean silent) {
        var collections = getCollection(sender, category, collectionId);
        if (collections == null) return;

        for (var collection : collections) {
            collection.setProgress(target, number);
        }

        if (!silent) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getSetSuccess(),
                    Placeholder.of("{player}", target.getName()),
                    Placeholder.of("{number}", AuroraAPI.formatNumber(number)),
                    Placeholder.of("{collection}", String.join(", ", collections.stream().map(Collection::getId).toList()))
            );
        }
    }

    @Subcommand("reset")
    @Description("Resets progress of a player's collection/category")
    @CommandCompletion("@players @categories|all @collections|all true|false")
    @CommandPermission("aurora.collections.admin.reset")
    public void onReset(CommandSender sender, @Flags("other") Player target, String category, @Default("all") String collectionId, @Default("false") Boolean silent) {
        var collections = getCollection(sender, category, collectionId);
        if (collections == null) return;

        for (var collection : collections) {
            collection.resetProgress(target);
        }

        if (!silent) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getResetSuccess(),
                    Placeholder.of("{player}", target.getName()),
                    Placeholder.of("{collection}", String.join(", ", collections.stream().map(Collection::getId).toList()))
            );
        }
    }

    private List<Collection> getCollection(CommandSender sender, String category, String collectionId) {
        if (category.equals("all")) {
            return plugin.getCollectionManager().getAllCollections();
        }

        if (!plugin.getCollectionManager().hasCategory(category)) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getCategoryNotFound(), Placeholder.of("{category}", category));
            return null;
        }

        if (collectionId.equals("all")) {
            return plugin.getCollectionManager().getCollectionsByCategory(category);
        }

        var collection = plugin.getCollectionManager().getCollection(category, collectionId);

        if (collection == null) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getCollectionNotFound(), Placeholder.of("{collection}", collectionId));
            return null;
        }

        return List.of(collection);
    }
}
