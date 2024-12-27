package gg.auroramc.collections.collection;

import com.google.common.collect.Lists;
import gg.auroramc.aurora.api.reward.Reward;
import gg.auroramc.aurora.api.reward.RewardFactory;
import gg.auroramc.collections.config.CategoriesConfig;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
public class Category {
    private final CategoriesConfig.CategoryConfig config;
    private final List<CategoryReward> rewards = Lists.newArrayList();
    private final boolean levelingEnabled;
    private final String id;

    public Category(String id, RewardFactory rewardFactory, CategoriesConfig.CategoryConfig config) {
        this.id = id;
        this.config = config;

        if (config.getLevels() == null || config.getLevels().isEmpty()) {
            levelingEnabled = false;
            return;
        }

        levelingEnabled = true;

        for (var levelConfig : config.getLevels().values()) {
            var parsedRewards = new ArrayList<Reward>();

            for (var key : levelConfig.getRewards().getKeys(false)) {
                var reward = rewardFactory.createReward(levelConfig.getRewards().getConfigurationSection(key));
                reward.ifPresent(parsedRewards::add);
            }

            rewards.add(new CategoryReward(levelConfig.getPercentage(), parsedRewards, levelConfig.getItem()));
        }

        rewards.sort(Comparator.comparingDouble(CategoryReward::percentage));
    }

    public List<Reward> getRewards(int prevLevel, int newLevel, int totalLevels) {
        var rewards = new ArrayList<Reward>();

        for (var reward : this.rewards) {
            if (reward.percentage() > (prevLevel / (double) totalLevels * 100)
                    && reward.percentage() <= (newLevel / (double) totalLevels * 100)) {
                rewards.addAll(reward.rewards());
            }
        }

        return rewards;
    }

    public List<Reward> getRewards(int level, int totalLevels) {
        var rewards = new ArrayList<Reward>();

        for (var reward : this.rewards) {
            if (reward.percentage() <= (level / (double) totalLevels * 100)) {
                rewards.addAll(reward.rewards());
            }
        }

        return rewards;
    }

    public boolean hasPermission(Player player) {
        return config.getPermission() == null || player.hasPermission(config.getPermission());
    }
}
