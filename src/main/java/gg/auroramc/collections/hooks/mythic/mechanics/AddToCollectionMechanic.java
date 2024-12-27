package gg.auroramc.collections.hooks.mythic.mechanics;

import com.google.common.collect.Maps;
import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.api.data.CollectionData;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;
import org.bukkit.entity.Player;

@MythicMechanic(
        author = "erik_sz",
        name = "addToCollection",
        description = "Add some progression to a certain collection"
)
public class AddToCollectionMechanic implements ITargetedEntitySkill {
    private final AuroraCollections plugin;
    private final String category;
    private final String collectionId;
    private final PlaceholderInt amount;

    public AddToCollectionMechanic(AuroraCollections plugin, MythicMechanicLoadEvent loader) {
        this.plugin = plugin;
        this.amount = loader.getConfig().getPlaceholderInteger(new String[]{"amount", "a",}, 0);
        this.category = loader.getConfig().getString(new String[]{"category", "cat", "ca"});
        this.collectionId = loader.getConfig().getString(new String[]{"collection", "col", "co"});

        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        if (collectionId == null) {
            throw new IllegalArgumentException("Collection cannot be null");
        }
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity target) {
        if (!target.isPlayer()) return SkillResult.INVALID_TARGET;
        Player player = BukkitAdapter.adapt(target.asPlayer());

        var user = AuroraAPI.getUserManager().getUser(player);
        if (!user.isLoaded()) return SkillResult.CONDITION_FAILED;

        var collection = plugin.getCollectionManager().getCollection(category, collectionId);

        if (collection == null) {
            return SkillResult.INVALID_CONFIG;
        }

        if (collection.hasPermission(player)) {
            collection.progress(player, null, amount.get(skillMetadata), null);
        }

        return SkillResult.SUCCESS;
    }
}
