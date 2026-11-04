package gg.auroramc.collections.reward.corrector;

import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.reward.CommandReward;
import gg.auroramc.aurora.api.reward.RewardCorrector;
import gg.auroramc.collections.AuroraCollections;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandCorrector implements RewardCorrector {
    private final AuroraCollections plugin;

    public CommandCorrector(AuroraCollections plugin) {
        this.plugin = plugin;
    }

    private record CommandPair(CommandReward reward, int level, List<Placeholder<?>> placeholders) {
    }

    @Override
    public void correctRewards(Player player) {
        var manager = plugin.getCollectionManager();
        final var rewards = new ArrayList<CommandPair>();

        for (var collection : manager.getAllCollections()) {
            var level = collection.getPlayerLevel(player);

            for (int i = 1; i < level + 1; i++) {
                var matcher = collection.getLevelMatcher().getBestMatcher(i);
                if (matcher == null) continue;

                for (var reward : matcher.computeRewards(i)) {
                    if (reward instanceof CommandReward commandReward) {
                        if (commandReward.shouldBeCorrected(player, i)) {
                            rewards.add(new CommandPair(commandReward, i, collection.getPlaceholders(player, i)));
                        }
                    }
                }
            }
        }

        if (rewards.isEmpty()) return;

        // On the player's own thread, the actions touch the player (on Folia only that thread may)
        player.getScheduler().run(plugin, (task) -> {
            rewards.forEach((reward) -> {
                if (!player.isOnline()) return;
                reward.reward().execute(player, reward.level(), reward.placeholders());
            });
            AuroraCollections.logger().debug("Corrected %d command rewards for player %s".formatted(rewards.size(), player.getName()));
        }, null);
    }
}
