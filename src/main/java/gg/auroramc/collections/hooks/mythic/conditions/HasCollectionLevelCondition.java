package gg.auroramc.collections.hooks.mythic.conditions;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.collections.AuroraCollections;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.conditions.ICasterCondition;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.core.utils.annotations.MythicCondition;
import org.bukkit.entity.Player;

@MythicCondition(
        author = "erik_sz",
        name = "hasCollectionLevel",
        description = "Check if the player has a certain Collections level",
        aliases = {"hasCollectionLvl"}
)
public class HasCollectionLevelCondition implements IEntityCondition, ICasterCondition {
    private final AuroraCollections plugin;
    private final PlaceholderInt level;
    private final String category;
    private final String collection;

    public HasCollectionLevelCondition(AuroraCollections plugin, MythicConditionLoadEvent loader) {
        this.plugin = plugin;
        this.level = loader.getConfig().getPlaceholderInteger(new String[]{"level", "lvl", "l"}, 0);
        this.category = loader.getConfig().getString(new String[]{"category", "cat", "ca"});
        this.collection = loader.getConfig().getString(new String[]{"collection", "col", "co"});

        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        if (collection == null) {
            throw new IllegalArgumentException("Collection cannot be null");
        }
    }

    @Override
    public boolean check(AbstractEntity entity) {
        return checkCondition(entity, level.get(entity));
    }

    @Override
    public boolean check(SkillCaster caster) {
        return checkCondition(caster.getEntity(), level.get(caster));
    }

    private boolean checkCondition(AbstractEntity entity, int level) {
        if (!entity.isPlayer()) return false;
        Player player = BukkitAdapter.adapt(entity.asPlayer());


        var user = AuroraAPI.getUserManager().getUser(player);
        if (!user.isLoaded()) return false;

        var collection = plugin.getCollectionManager().getCollection(category, this.collection);
        if (collection == null) return false;

        return collection.getPlayerLevel(player) >= level;
    }
}
