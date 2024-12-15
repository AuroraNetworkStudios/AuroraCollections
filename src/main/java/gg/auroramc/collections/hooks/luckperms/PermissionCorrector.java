package gg.auroramc.collections.hooks.luckperms;

import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.reward.PermissionReward;
import gg.auroramc.aurora.api.reward.RewardCorrector;
import gg.auroramc.collections.AuroraCollections;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeEqualityPredicate;
import net.luckperms.api.util.Tristate;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PermissionCorrector implements RewardCorrector {
    private final AuroraCollections plugin;

    public PermissionCorrector(AuroraCollections plugin) {
        this.plugin = plugin;
    }

    @Override
    public void correctRewards(Player player) {
        var manager = plugin.getCollectionManager();

        List<Node> nodesToAdd = new ArrayList<>();

        for (var collection : manager.getAllCollections()) {
            var level = collection.getPlayerLevel(player);

            for (int i = 1; i < level + 1; i++) {
                var matcher = collection.getLevelMatcher().getBestMatcher(i);
                if (matcher == null) continue;
                var placeholders = collection.getPlaceholders(player, i);
                for (var reward : matcher.computeRewards(i)) {
                    if (reward instanceof PermissionReward permissionReward) {
                        nodesToAdd.addAll(getNodes(player, permissionReward, placeholders));
                    }
                }
            }
        }

        for (var category : manager.getCategories()) {
            if (!category.isLevelingEnabled()) continue;
            var rewards = category.getRewards(manager.getCategoryLevel(category.getId(), player), manager.getMaxCategoryLevel(category.getId()));

            List<Placeholder<?>> placeholders = List.of(
                    Placeholder.of("{player}", player.getName()),
                    Placeholder.of("{category_name}", category.getConfig().getName()),
                    Placeholder.of("{category_id}", category.getId())
            );

            for (var reward : rewards) {
                if (reward instanceof PermissionReward permissionReward) {
                    nodesToAdd.addAll(getNodes(player, permissionReward, placeholders));
                }
            }
        }

        if (nodesToAdd.isEmpty()) return;

        var user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            AuroraCollections.logger().severe("User " + player.getName() + " is not loaded in LuckPerms, failed to correct permission rewards!");
            return;
        }

        int addedNodes = 0;
        for (var node : nodesToAdd) {
            var hasPermission = user.data().contains(node, NodeEqualityPredicate.EXACT);

            if (hasPermission.equals(Tristate.UNDEFINED)) {
                AuroraCollections.logger().debug("Permission " + node.getKey() + " is undefined for player " + player.getName());
                user.data().add(node);
                addedNodes++;
            }
        }

        if (addedNodes > 0) {
            LuckPermsProvider.get().getUserManager().saveUser(user);
            AuroraCollections.logger().debug("Added " + addedNodes + " permission nodes to player " + player.getName());
        }
    }

    private List<Node> getNodes(Player player, PermissionReward permissionReward, List<Placeholder<?>> placeholders) {
        if (permissionReward.getPermissions() == null || permissionReward.getPermissions().isEmpty())
            return List.of();

        return permissionReward.buildNodes(player, placeholders);
    }
}
