package gg.auroramc.collections.hooks.pyrofishing;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.collections.AuroraCollections;
import gg.auroramc.collections.collection.Trigger;
import gg.auroramc.collections.hooks.Hook;
import me.arsmagica.API.PyroFishCatchEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PyroFishingHook implements Hook, Listener {
    private AuroraCollections plugin;

    @Override
    public void hook(AuroraCollections plugin) {
        this.plugin = plugin;
        AuroraCollections.logger().info("Hooked into PyroFishingPro for custom fish loot.");
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFish(PyroFishCatchEvent event) {
        plugin.getCollectionManager().progressCollections(
                event.getPlayer(),
                new TypeId("pyrofishing", event.getTier()),
                event.getFishNumber(),
                Trigger.FISH
        );
    }
}
